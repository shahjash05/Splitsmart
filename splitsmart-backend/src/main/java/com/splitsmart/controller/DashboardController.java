package com.splitsmart.controller;

import com.splitsmart.dto.response.BalanceDTO;
import com.splitsmart.dto.response.DashboardDTO;
import com.splitsmart.dto.response.ExpenseDTO;
import com.splitsmart.exception.ResourceNotFoundException;
import com.splitsmart.model.Group;
import com.splitsmart.model.User;
import com.splitsmart.repository.GroupRepository;
import com.splitsmart.service.BalanceService;
import com.splitsmart.service.ExpenseService;
import com.splitsmart.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final ExpenseService expenseService;
    private final BalanceService balanceService;
    private final GroupService groupService;
    private final GroupRepository groupRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard(@AuthenticationPrincipal User user) {
        BigDecimal monthlyTotal = expenseService.getMonthlyTotal(user);
        BigDecimal limit = user.getMonthlySpendingLimit();

        double usedPct = 0.0;
        boolean nearLimit = false;
        if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
            usedPct = monthlyTotal.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            nearLimit = usedPct >= 80.0;
        }

        List<BalanceDTO> balances = balanceService.getGlobalBalances(user);
        BigDecimal totalOwed = balances.stream()
                .filter(b -> b.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(BalanceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOwing = balances.stream()
                .filter(b -> b.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(b -> b.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ExpenseDTO> recent = expenseService.getUserExpenses(user, null, null, null)
                .stream().limit(5).toList();

        int activeGroups = groupService.getUserGroups(user).size();

        DashboardDTO dto = DashboardDTO.builder()
                .totalExpensesThisMonth(monthlyTotal)
                .totalOwed(totalOwed)
                .totalOwing(totalOwing)
                .activeGroups(activeGroups)
                .monthlyLimit(limit)
                .limitUsedPercentage(usedPct)
                .nearLimit(nearLimit)
                .recentExpenses(recent)
                .balances(balances)
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/balances")
    public ResponseEntity<List<BalanceDTO>> getGlobalBalances(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(balanceService.getGlobalBalances(user));
    }

    @GetMapping("/groups/{groupId}/balances")
    public ResponseEntity<List<BalanceDTO>> getGroupBalances(@AuthenticationPrincipal User user,
                                                              @PathVariable Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
        return ResponseEntity.ok(balanceService.getGroupBalances(group, user));
    }
}
