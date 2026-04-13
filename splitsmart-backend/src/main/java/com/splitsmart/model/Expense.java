package com.splitsmart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "date_of_expense", nullable = false)
    private LocalDate dateOfExpense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseParticipation> participations = new ArrayList<>();

    public Expense() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDate getDateOfExpense() { return dateOfExpense; }
    public User getCreatedBy() { return createdBy; }
    public Category getCategory() { return category; }
    public Group getGroup() { return group; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<ExpenseParticipation> getParticipations() { return participations; }

    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }
    public void setDescription(String description) { this.description = description; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setDateOfExpense(LocalDate dateOfExpense) { this.dateOfExpense = dateOfExpense; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setCategory(Category category) { this.category = category; }
    public void setGroup(Group group) { this.group = group; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setParticipations(List<ExpenseParticipation> participations) { this.participations = participations; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Expense e = new Expense();
        public Builder description(String v) { e.description = v; return this; }
        public Builder totalAmount(BigDecimal v) { e.totalAmount = v; return this; }
        public Builder dateOfExpense(LocalDate v) { e.dateOfExpense = v; return this; }
        public Builder createdBy(User v) { e.createdBy = v; return this; }
        public Builder category(Category v) { e.category = v; return this; }
        public Builder group(Group v) { e.group = v; return this; }
        public Builder participations(List<ExpenseParticipation> v) { e.participations = v; return this; }
        public Expense build() { return e; }
    }
}
