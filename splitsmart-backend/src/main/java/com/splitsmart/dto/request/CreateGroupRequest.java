package com.splitsmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    @NotBlank
    private String groupName;
    private List<String> memberEmails; // emails of members to add
}
