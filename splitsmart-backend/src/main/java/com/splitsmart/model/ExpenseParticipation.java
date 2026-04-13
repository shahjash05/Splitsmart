package com.splitsmart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_participations")
public class ExpenseParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_id")
    private Long participationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "owed_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal owedAmount;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    public ExpenseParticipation() {}

    public Long getParticipationId() { return participationId; }
    public Expense getExpense() { return expense; }
    public User getUser() { return user; }
    public BigDecimal getOwedAmount() { return owedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }

    public void setParticipationId(Long participationId) { this.participationId = participationId; }
    public void setExpense(Expense expense) { this.expense = expense; }
    public void setUser(User user) { this.user = user; }
    public void setOwedAmount(BigDecimal owedAmount) { this.owedAmount = owedAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ExpenseParticipation p = new ExpenseParticipation();
        public Builder expense(Expense v) { p.expense = v; return this; }
        public Builder user(User v) { p.user = v; return this; }
        public Builder owedAmount(BigDecimal v) { p.owedAmount = v; return this; }
        public Builder paidAmount(BigDecimal v) { p.paidAmount = v; return this; }
        public ExpenseParticipation build() { return p; }
    }
}
