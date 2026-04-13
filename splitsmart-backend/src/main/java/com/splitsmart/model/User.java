package com.splitsmart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "monthly_spending_limit", precision = 10, scale = 2)
    private BigDecimal monthlySpendingLimit;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public BigDecimal getMonthlySpendingLimit() { return monthlySpendingLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setMonthlySpendingLimit(BigDecimal monthlySpendingLimit) { this.monthlySpendingLimit = monthlySpendingLimit; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final User user = new User();
        public Builder name(String v) { user.name = v; return this; }
        public Builder email(String v) { user.email = v; return this; }
        public Builder password(String v) { user.password = v; return this; }
        public Builder monthlySpendingLimit(BigDecimal v) { user.monthlySpendingLimit = v; return this; }
        public User build() { return user; }
    }
}
