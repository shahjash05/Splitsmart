package com.splitsmart.service;

import com.splitsmart.dto.request.CreateGroupRequest;
import com.splitsmart.dto.request.UpdateMembersRequest;
import com.splitsmart.dto.response.GroupDTO;
import com.splitsmart.exception.BadRequestException;
import com.splitsmart.exception.ResourceNotFoundException;
import com.splitsmart.exception.UnauthorizedException;
import com.splitsmart.model.Group;
import com.splitsmart.model.GroupMembership;
import com.splitsmart.model.User;
import com.splitsmart.repository.ExpenseRepository;
import com.splitsmart.repository.GroupMembershipRepository;
import com.splitsmart.repository.GroupRepository;
import com.splitsmart.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public GroupDTO createGroup(User creator, CreateGroupRequest req) {
        Group group = Group.builder()
                .groupName(req.getGroupName())
                .createdBy(creator)
                .build();
        groupRepository.save(group);

        // Add creator as first member
        membershipRepository.save(GroupMembership.builder().user(creator).group(group).build());

        // Add additional members
        if (req.getMemberEmails() != null) {
            for (String email : req.getMemberEmails()) {
                if (email.equals(creator.getEmail())) continue;
                userRepository.findByEmail(email).ifPresent(u ->
                    membershipRepository.save(GroupMembership.builder().user(u).group(group).build())
                );
            }
        }

        return toDTO(group, creator);
    }

    public List<GroupDTO> getUserGroups(User user) {
        return groupRepository.findGroupsByMember(user).stream()
                .map(g -> toDTO(g, user))
                .collect(Collectors.toList());
    }

    public GroupDTO getGroupById(Long groupId, User currentUser) {
        Group group = findGroupOrThrow(groupId);
        assertMember(group, currentUser);
        return toDTO(group, currentUser);
    }

    @Transactional
    public GroupDTO updateMembers(Long groupId, User currentUser, UpdateMembersRequest req) {
        Group group = findGroupOrThrow(groupId);

        if (!group.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Only the group creator can edit members");
        }

        // Add new members
        if (req.getAddEmails() != null) {
            for (String email : req.getAddEmails()) {
                userRepository.findByEmail(email).ifPresent(u -> {
                    if (!membershipRepository.existsByUserAndGroup(u, group)) {
                        membershipRepository.save(GroupMembership.builder().user(u).group(group).build());
                    }
                });
            }
        }

        // Remove members (cannot remove creator)
        if (req.getRemoveUserIds() != null) {
            for (Long userId : req.getRemoveUserIds()) {
                if (userId.equals(group.getCreatedBy().getUserId())) {
                    throw new BadRequestException("Cannot remove the group creator");
                }
                userRepository.findById(userId).ifPresent(u ->
                    membershipRepository.deleteByUserAndGroup(u, group)
                );
            }
        }

        groupRepository.flush();
        Group refreshed = findGroupOrThrow(groupId);
        return toDTO(refreshed, currentUser);
    }

    @Transactional
    public void leaveGroup(Long groupId, User currentUser) {
        Group group = findGroupOrThrow(groupId);
        assertMember(group, currentUser);

        if (group.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            throw new BadRequestException("Group creator cannot leave. Delete the group instead.");
        }

        membershipRepository.deleteByUserAndGroup(currentUser, group);
    }

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
    }

    private void assertMember(Group group, User user) {
        if (!membershipRepository.existsByUserAndGroup(user, group)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
    }

    private GroupDTO toDTO(Group group, User currentUser) {
        var memberships = membershipRepository.findByGroup(group);
        var expenses = expenseRepository.findByGroup(group);
        double totalAmt = expenses.stream()
                .mapToDouble(e -> e.getTotalAmount().doubleValue())
                .sum();

        List<GroupDTO.MemberDTO> memberDTOs = memberships.stream()
                .map(m -> GroupDTO.MemberDTO.builder()
                        .userId(m.getUser().getUserId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .isCreator(m.getUser().getUserId().equals(group.getCreatedBy().getUserId()))
                        .build())
                .collect(Collectors.toList());

        return GroupDTO.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .createdById(group.getCreatedBy().getUserId())
                .createdByName(group.getCreatedBy().getName())
                .createdAt(group.getCreatedAt())
                .members(memberDTOs)
                .totalExpenses(expenses.size())
                .totalAmount(totalAmt)
                .build();
    }
}
