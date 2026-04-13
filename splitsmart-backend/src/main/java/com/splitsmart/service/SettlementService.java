package com.splitsmart.service;

import com.splitsmart.dto.request.CreateSettlementRequest;
import com.splitsmart.dto.response.SettlementDTO;
import com.splitsmart.exception.BadRequestException;
import com.splitsmart.exception.ResourceNotFoundException;
import com.splitsmart.exception.UnauthorizedException;
import com.splitsmart.model.Group;
import com.splitsmart.model.Settlement;
import com.splitsmart.model.User;
import com.splitsmart.repository.GroupRepository;
import com.splitsmart.repository.SettlementRepository;
import com.splitsmart.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public SettlementDTO create(User payer, CreateSettlementRequest req) {
        if (req.getReceiverId().equals(payer.getUserId())) {
            throw new BadRequestException("Payer and receiver cannot be the same user");
        }

        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Group group = null;
        if (req.getGroupId() != null) {
            group = groupRepository.findById(req.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        }

        Settlement settlement = Settlement.builder()
                .payer(payer)
                .receiver(receiver)
                .amount(req.getAmount())
                .status(Settlement.Status.PENDING)
                .group(group)
                .build();

        settlementRepository.save(settlement);
        return toDTO(settlement);
    }

    public List<SettlementDTO> getUserSettlements(User user, String status) {
        List<Settlement> settlements;
        if (status != null) {
            Settlement.Status st = Settlement.Status.valueOf(status.toUpperCase());
            settlements = settlementRepository.findByUserAndStatus(user, st);
        } else {
            settlements = settlementRepository.findByUser(user);
        }
        return settlements.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SettlementDTO confirm(Long id, User currentUser) {
        Settlement settlement = findOrThrow(id);
        assertReceiver(settlement, currentUser);
        if (settlement.getStatus() != Settlement.Status.PENDING) {
            throw new BadRequestException("Settlement is not in PENDING state");
        }
        settlement.setStatus(Settlement.Status.CONFIRMED);
        settlementRepository.save(settlement);
        return toDTO(settlement);
    }

    @Transactional
    public SettlementDTO reject(Long id, User currentUser) {
        Settlement settlement = findOrThrow(id);
        assertReceiver(settlement, currentUser);
        if (settlement.getStatus() != Settlement.Status.PENDING) {
            throw new BadRequestException("Settlement is not in PENDING state");
        }
        settlement.setStatus(Settlement.Status.REJECTED);
        settlementRepository.save(settlement);
        return toDTO(settlement);
    }

    private Settlement findOrThrow(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + id));
    }

    private void assertReceiver(Settlement settlement, User user) {
        if (!settlement.getReceiver().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("Only the receiver can confirm or reject this settlement");
        }
    }

    private SettlementDTO toDTO(Settlement s) {
        return SettlementDTO.builder()
                .settlementId(s.getSettlementId())
                .amount(s.getAmount())
                .status(s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .payerId(s.getPayer().getUserId())
                .payerName(s.getPayer().getName())
                .receiverId(s.getReceiver().getUserId())
                .receiverName(s.getReceiver().getName())
                .groupId(s.getGroup() != null ? s.getGroup().getGroupId() : null)
                .groupName(s.getGroup() != null ? s.getGroup().getGroupName() : null)
                .build();
    }
}
