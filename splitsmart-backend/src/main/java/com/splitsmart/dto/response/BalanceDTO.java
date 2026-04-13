package com.splitsmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BalanceDTO {
    private Long otherUserId;
    private String otherUserName;
    private BigDecimal amount;       // positive = they owe you, negative = you owe them
    private String direction;        // "OWED_TO_YOU" or "YOU_OWE"
}
