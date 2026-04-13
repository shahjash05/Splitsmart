package com.splitsmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateExpenseRequest {
    @NotBlank
    private String description;

    @NotNull @Positive
    private BigDecimal totalAmount;

    @NotNull
    private LocalDate dateOfExpense;

    @NotNull
    private Long categoryId;

    private Long groupId; // null = personal expense

    private String splitMethod; // "equal" or "manual"

    @NotNull
    private List<ParticipationRequest> participants;

    @Data
    public static class ParticipationRequest {
        @NotNull
        private Long userId;
        private BigDecimal owedAmount;  // used in manual split
        private BigDecimal paidAmount;  // how much this user paid
    }
}
