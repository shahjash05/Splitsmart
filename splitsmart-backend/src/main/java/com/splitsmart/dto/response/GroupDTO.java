package com.splitsmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GroupDTO {
    private Long groupId;
    private String groupName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<MemberDTO> members;
    private int totalExpenses;
    private double totalAmount;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MemberDTO {
        private Long userId;
        private String name;
        private String email;
        private boolean isCreator;
    }
}
