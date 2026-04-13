package com.splitsmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private BigDecimal monthlySpendingLimit;
}
