package com.splitsmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpenseDTO {
    private Long expenseId;
    private String description;
    private BigDecimal totalAmount;
    private LocalDate dateOfExpense;
    private LocalDateTime createdAt;

    private Long createdById;
    private String createdByName;

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;

    private Long groupId;
    private String groupName;

    private List<ParticipationDTO> participants;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ParticipationDTO {
        private Long userId;
        private String userName;
        private BigDecimal owedAmount;
        private BigDecimal paidAmount;
    }
}
