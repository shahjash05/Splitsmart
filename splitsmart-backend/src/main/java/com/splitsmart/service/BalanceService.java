package com.splitsmart.service;

import com.splitsmart.dto.response.BalanceDTO;
import com.splitsmart.model.*;
import com.splitsmart.repository.ExpenseRepository;
import com.splitsmart.repository.GroupMembershipRepository;
import com.splitsmart.repository.GroupRepository;
import com.splitsmart.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;

    /**
     * Calculate net balance between two users (optionally scoped to a group).
     * Positive = user1 is owed money by user2.
     * Negative = user1 owes money to user2.
     */
    public BigDecimal calculateBalance(User user1, User user2, Group group) {
        List<Expense> expenses = (group != null)
                ? expenseRepository.findByGroup(group)
                : expenseRepository.findByParticipant(user1);

        BigDecimal balance = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            Optional<ExpenseParticipation> p1Opt = expense.getParticipations().stream()
                    .filter(p -> p.getUser().getUserId().equals(user1.getUserId())).findFirst();
            Optional<ExpenseParticipation> p2Opt = expense.getParticipations().stream()
                    .filter(p -> p.getUser().getUserId().equals(user2.getUserId())).findFirst();

            if (p1Opt.isEmpty() || p2Opt.isEmpty()) continue;

            ExpenseParticipation p1 = p1Opt.get();
            ExpenseParticipation p2 = p2Opt.get();
            BigDecimal total = expense.getTotalAmount();

            if (total.compareTo(BigDecimal.ZERO) == 0) continue;

            // User1 paid proportion for user2
            BigDecimal u1PaidForU2 = p1.getPaidAmount()
                    .divide(total, 10, RoundingMode.HALF_UP)
                    .multiply(p2.getOwedAmount());

            // User2 paid proportion for user1
            BigDecimal u2PaidForU1 = p2.getPaidAmount()
                    .divide(total, 10, RoundingMode.HALF_UP)
                    .multiply(p1.getOwedAmount());

            balance = balance.add(u1PaidForU2).subtract(u2PaidForU1);
        }

        // Apply confirmed settlements
        List<Settlement> settlements = settlementRepository.findConfirmedBetween(user1, user2, Settlement.Status.CONFIRMED);
        for (Settlement s : settlements) {
            if (s.getPayer().getUserId().equals(user2.getUserId())) {
                // user2 paid user1 → user1 is owed more
                balance = balance.add(s.getAmount());
            } else {
                balance = balance.subtract(s.getAmount());
            }
        }

        return balance.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get all non-zero balances for a user (globally).
     */
    public List<BalanceDTO> getGlobalBalances(User user) {
        // Find all users this person shares expenses with
        List<Expense> allExpenses = expenseRepository.findByParticipant(user);
        Set<Long> involvedUserIds = new HashSet<>();
        for (Expense e : allExpenses) {
            for (ExpenseParticipation p : e.getParticipations()) {
                if (!p.getUser().getUserId().equals(user.getUserId())) {
                    involvedUserIds.add(p.getUser().getUserId());
                }
            }
        }

        List<BalanceDTO> result = new ArrayList<>();
        for (Expense e : allExpenses) {
            for (ExpenseParticipation p : e.getParticipations()) {
                User other = p.getUser();
                if (!other.getUserId().equals(user.getUserId()) && involvedUserIds.contains(other.getUserId())) {
                    involvedUserIds.remove(other.getUserId());
                    BigDecimal amount = calculateBalance(user, other, null);
                    if (amount.abs().compareTo(new BigDecimal("0.01")) > 0) {
                        result.add(BalanceDTO.builder()
                                .otherUserId(other.getUserId())
                                .otherUserName(other.getName())
                                .amount(amount)
                                .direction(amount.compareTo(BigDecimal.ZERO) > 0 ? "OWED_TO_YOU" : "YOU_OWE")
                                .build());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get balances within a specific group.
     */
    public List<BalanceDTO> getGroupBalances(Group group, User currentUser) {
        List<GroupMembership> memberships = membershipRepository.findByGroup(group);
        List<User> members = memberships.stream().map(GroupMembership::getUser).collect(Collectors.toList());

        List<BalanceDTO> result = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {
                User u1 = members.get(i);
                User u2 = members.get(j);
                BigDecimal balance = calculateBalance(u1, u2, group);
                if (balance.abs().compareTo(new BigDecimal("0.01")) > 0) {
                    // Express from current user's perspective
                    if (u1.getUserId().equals(currentUser.getUserId())) {
                        result.add(BalanceDTO.builder()
                                .otherUserId(u2.getUserId()).otherUserName(u2.getName())
                                .amount(balance)
                                .direction(balance.compareTo(BigDecimal.ZERO) > 0 ? "OWED_TO_YOU" : "YOU_OWE")
                                .build());
                    } else if (u2.getUserId().equals(currentUser.getUserId())) {
                        result.add(BalanceDTO.builder()
                                .otherUserId(u1.getUserId()).otherUserName(u1.getName())
                                .amount(balance.negate())
                                .direction(balance.compareTo(BigDecimal.ZERO) < 0 ? "OWED_TO_YOU" : "YOU_OWE")
                                .build());
                    }
                }
            }
        }
        return result;
    }
}
