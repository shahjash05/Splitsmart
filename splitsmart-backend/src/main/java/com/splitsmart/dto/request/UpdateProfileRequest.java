package com.splitsmart.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProfileRequest {
    private String name;
    private String username;
    private String email;
    private BigDecimal monthlySpendingLimit;
}
