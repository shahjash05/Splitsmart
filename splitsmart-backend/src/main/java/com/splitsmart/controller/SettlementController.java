package com.splitsmart.controller;

import com.splitsmart.dto.request.CreateSettlementRequest;
import com.splitsmart.dto.response.SettlementDTO;
import com.splitsmart.model.User;
import com.splitsmart.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // GET /api/settlements?status=PENDING|CONFIRMED|REJECTED
    @GetMapping
    public ResponseEntity<List<SettlementDTO>> getSettlements(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(settlementService.getUserSettlements(user, status));
    }

    @PostMapping
    public ResponseEntity<SettlementDTO> createSettlement(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody CreateSettlementRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementService.create(user, req));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<SettlementDTO> confirm(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(settlementService.confirm(id, user));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<SettlementDTO> reject(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(settlementService.reject(id, user));
    }
}
