package com.splitsmart.controller;

import com.splitsmart.dto.request.CreateExpenseRequest;
import com.splitsmart.dto.response.ExpenseDTO;
import com.splitsmart.model.User;
import com.splitsmart.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // GET /api/expenses?type=all|personal|group&groupId=&categoryId=
    @GetMapping("/api/expenses")
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(expenseService.getUserExpenses(user, type, groupId, categoryId));
    }

    @PostMapping("/api/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody CreateExpenseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(user, req));
    }

    @GetMapping("/api/expenses/{id}")
    public ResponseEntity<ExpenseDTO> getExpense(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id, user));
    }

    @PutMapping("/api/expenses/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@AuthenticationPrincipal User user,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody CreateExpenseRequest req) {
        return ResponseEntity.ok(expenseService.updateExpense(id, user, req));
    }

    @DeleteMapping("/api/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@AuthenticationPrincipal User user, @PathVariable Long id) {
        expenseService.deleteExpense(id, user);
        return ResponseEntity.noContent().build();
    }

    // GET /api/groups/{id}/expenses
    @GetMapping("/api/groups/{groupId}/expenses")
    public ResponseEntity<List<ExpenseDTO>> getGroupExpenses(@AuthenticationPrincipal User user,
                                                              @PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId, user));
    }
}
