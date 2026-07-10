package com.splitsmart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSettlementRequest {
    private Long receiverId; // For "Pay To" (current user is payer)
    private Long payerId;    // For "Take From" (current user is receiver)

    @NotNull @Positive
    private BigDecimal amount;

    private Long groupId; // null = global settlement
}
