package com.splitsmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SettlementDTO {
    private Long settlementId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    private Long payerId;
    private String payerName;

    private Long receiverId;
    private String receiverName;

    private Long groupId;
    private String groupName;
}
