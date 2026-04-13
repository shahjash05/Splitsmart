package com.splitsmart.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UpdateMembersRequest {
    private List<String> addEmails;    // emails to add
    private List<Long> removeUserIds;  // user IDs to remove
}
