package com.splitsmart.controller;

import com.splitsmart.dto.request.ChangePasswordRequest;
import com.splitsmart.dto.request.UpdateProfileRequest;
import com.splitsmart.dto.response.UserDTO;
import com.splitsmart.model.User;
import com.splitsmart.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@AuthenticationPrincipal User user,
                                                  @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(user, req));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                               @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(user, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<UserDTO> searchByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.searchByEmail(email));
    }
}
