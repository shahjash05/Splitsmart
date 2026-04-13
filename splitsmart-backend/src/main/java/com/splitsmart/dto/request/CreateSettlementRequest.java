package com.splitsmart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSettlementRequest {
    @NotNull
    private Long receiverId;

    @NotNull @Positive
    private BigDecimal amount;

    private Long groupId; // null = global settlement
}
