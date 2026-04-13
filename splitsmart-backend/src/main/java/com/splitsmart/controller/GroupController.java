package com.splitsmart.controller;

import com.splitsmart.dto.request.CreateGroupRequest;
import com.splitsmart.dto.request.UpdateMembersRequest;
import com.splitsmart.dto.response.GroupDTO;
import com.splitsmart.model.User;
import com.splitsmart.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getMyGroups(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.getUserGroups(user));
    }

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody CreateGroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(user, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id, user));
    }

    @PutMapping("/{id}/members")
    public ResponseEntity<GroupDTO> updateMembers(@AuthenticationPrincipal User user,
                                                   @PathVariable Long id,
                                                   @RequestBody UpdateMembersRequest req) {
        return ResponseEntity.ok(groupService.updateMembers(id, user, req));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(@AuthenticationPrincipal User user, @PathVariable Long id) {
        groupService.leaveGroup(id, user);
        return ResponseEntity.noContent().build();
    }
}
