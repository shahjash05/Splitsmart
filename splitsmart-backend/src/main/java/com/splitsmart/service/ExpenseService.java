package com.splitsmart.service;

import com.splitsmart.dto.request.CreateExpenseRequest;
import com.splitsmart.dto.response.ExpenseDTO;
import com.splitsmart.exception.BadRequestException;
import com.splitsmart.exception.ResourceNotFoundException;
import com.splitsmart.exception.UnauthorizedException;
import com.splitsmart.model.*;
import com.splitsmart.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipationRepository participationRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseDTO createExpense(User creator, CreateExpenseRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Group group = null;
        if (req.getGroupId() != null) {
            group = groupRepository.findById(req.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
            // Validate creator is a member
            if (!membershipRepository.existsByUserAndGroup(creator, group)) {
                throw new UnauthorizedException("You are not a member of this group");
            }
        }

        Expense expense = Expense.builder()
                .description(req.getDescription())
                .totalAmount(req.getTotalAmount())
                .dateOfExpense(req.getDateOfExpense())
                .createdBy(creator)
                .category(category)
                .group(group)
                .participations(new ArrayList<>())
                .build();

        expenseRepository.save(expense);

        // Set participations
        List<ExpenseParticipation> participations = buildParticipations(expense, req);
        participationRepository.saveAll(participations);
        expense.setParticipations(participations);

        return toDTO(expense);
    }

    public List<ExpenseDTO> getUserExpenses(User user, String type, Long groupId, Long categoryId) {
        List<Expense> expenses;

        if ("personal".equalsIgnoreCase(type)) {
            expenses = expenseRepository.findByGroupIsNullAndCreatedBy(user);
        } else if ("group".equalsIgnoreCase(type)) {
            expenses = expenseRepository.findByParticipant(user).stream()
                    .filter(e -> e.getGroup() != null)
                    .collect(Collectors.toList());
        } else {
            expenses = expenseRepository.findByParticipant(user);
        }

        if (groupId != null) {
            expenses = expenses.stream().filter(e -> e.getGroup() != null &&
                    e.getGroup().getGroupId().equals(groupId)).collect(Collectors.toList());
        }
        if (categoryId != null) {
            expenses = expenses.stream().filter(e ->
                    e.getCategory().getCategoryId().equals(categoryId)).collect(Collectors.toList());
        }

        return expenses.stream()
                .sorted((a, b) -> b.getDateOfExpense().compareTo(a.getDateOfExpense()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getGroupExpenses(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        if (!membershipRepository.existsByUserAndGroup(currentUser, group)) {
            throw new UnauthorizedException("Not a member of this group");
        }
        return expenseRepository.findByGroup(group).stream()
                .sorted((a, b) -> b.getDateOfExpense().compareTo(a.getDateOfExpense()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id, User currentUser) {
        Expense expense = findOrThrow(id);
        assertParticipant(expense, currentUser);
        return toDTO(expense);
    }

    @Transactional
    public ExpenseDTO updateExpense(Long id, User currentUser, CreateExpenseRequest req) {
        Expense expense = findOrThrow(id);
        if (!expense.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Only the expense creator can edit it");
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        expense.setDescription(req.getDescription());
        expense.setTotalAmount(req.getTotalAmount());
        expense.setDateOfExpense(req.getDateOfExpense());
        expense.setCategory(category);

        participationRepository.deleteAll(expense.getParticipations());
        expense.getParticipations().clear();

        List<ExpenseParticipation> participations = buildParticipations(expense, req);
        participationRepository.saveAll(participations);
        expense.setParticipations(participations);
        expenseRepository.save(expense);

        return toDTO(expense);
    }

    @Transactional
    public void deleteExpense(Long id, User currentUser) {
        Expense expense = findOrThrow(id);
        if (!expense.getCreatedBy().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedException("Only the expense creator can delete it");
        }
        expenseRepository.delete(expense);
    }

    public BigDecimal getMonthlyTotal(User user) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return expenseRepository.findByParticipantAndDateRange(user, start, end).stream()
                .map(e -> e.getParticipations().stream()
                        .filter(p -> p.getUser().getUserId().equals(user.getUserId()))
                        .map(ExpenseParticipation::getOwedAmount)
                        .findFirst().orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private List<ExpenseParticipation> buildParticipations(Expense expense, CreateExpenseRequest req) {
        List<ExpenseParticipation> result = new ArrayList<>();
        boolean isEqual = !"manual".equalsIgnoreCase(req.getSplitMethod());

        if (isEqual) {
            int count = req.getParticipants().size();
            BigDecimal share = expense.getTotalAmount().divide(
                    BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

            for (CreateExpenseRequest.ParticipationRequest pr : req.getParticipants()) {
                User u = userRepository.findById(pr.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + pr.getUserId()));
                result.add(ExpenseParticipation.builder()
                        .expense(expense).user(u)
                        .owedAmount(share)
                        .paidAmount(pr.getPaidAmount() != null ? pr.getPaidAmount() : BigDecimal.ZERO)
                        .build());
            }
        } else {
            // Manual split — validate totals
            BigDecimal owedTotal = req.getParticipants().stream()
                    .map(p -> p.getOwedAmount() != null ? p.getOwedAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (owedTotal.setScale(2, RoundingMode.HALF_UP)
                    .compareTo(expense.getTotalAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
                throw new BadRequestException("Sum of owed amounts (" + owedTotal +
                        ") must equal total amount (" + expense.getTotalAmount() + ")");
            }

            for (CreateExpenseRequest.ParticipationRequest pr : req.getParticipants()) {
                User u = userRepository.findById(pr.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + pr.getUserId()));
                result.add(ExpenseParticipation.builder()
                        .expense(expense).user(u)
                        .owedAmount(pr.getOwedAmount() != null ? pr.getOwedAmount() : BigDecimal.ZERO)
                        .paidAmount(pr.getPaidAmount() != null ? pr.getPaidAmount() : BigDecimal.ZERO)
                        .build());
            }
        }
        return result;
    }

    private Expense findOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    private void assertParticipant(Expense expense, User user) {
        boolean isParticipant = expense.getParticipations().stream()
                .anyMatch(p -> p.getUser().getUserId().equals(user.getUserId()));
        if (!isParticipant && !expense.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("Access denied for this expense");
        }
    }

    public ExpenseDTO toDTO(Expense expense) {
        List<ExpenseDTO.ParticipationDTO> parts = expense.getParticipations().stream()
                .map(p -> ExpenseDTO.ParticipationDTO.builder()
                        .userId(p.getUser().getUserId())
                        .userName(p.getUser().getName())
                        .owedAmount(p.getOwedAmount())
                        .paidAmount(p.getPaidAmount())
                        .build())
                .collect(Collectors.toList());

        return ExpenseDTO.builder()
                .expenseId(expense.getExpenseId())
                .description(expense.getDescription())
                .totalAmount(expense.getTotalAmount())
                .dateOfExpense(expense.getDateOfExpense())
                .createdAt(expense.getCreatedAt())
                .createdById(expense.getCreatedBy().getUserId())
                .createdByName(expense.getCreatedBy().getName())
                .categoryId(expense.getCategory().getCategoryId())
                .categoryName(expense.getCategory().getCategoryName())
                .categoryIcon(expense.getCategory().getIcon())
                .groupId(expense.getGroup() != null ? expense.getGroup().getGroupId() : null)
                .groupName(expense.getGroup() != null ? expense.getGroup().getGroupName() : null)
                .participants(parts)
                .build();
    }
}
