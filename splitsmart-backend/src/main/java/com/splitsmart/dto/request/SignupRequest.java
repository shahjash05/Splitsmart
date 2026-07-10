package com.splitsmart.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    private String name;
    @NotBlank @Size(min = 3, max = 30)
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6)
    private String password;
}
