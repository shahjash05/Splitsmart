package com.splitsmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardDTO {
    private BigDecimal totalExpensesThisMonth;
    private BigDecimal totalOwed;      // others owe you
    private BigDecimal totalOwing;     // you owe others
    private int activeGroups;
    private BigDecimal monthlyLimit;
    private double limitUsedPercentage;
    private boolean nearLimit;
    private List<ExpenseDTO> recentExpenses;
    private List<BalanceDTO> balances;
}
