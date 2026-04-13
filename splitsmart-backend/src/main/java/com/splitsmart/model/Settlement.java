package com.splitsmart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class Settlement {

    public enum Status { PENDING, CONFIRMED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long settlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Settlement() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getSettlementId() { return settlementId; }
    public User getPayer() { return payer; }
    public User getReceiver() { return receiver; }
    public BigDecimal getAmount() { return amount; }
    public Status getStatus() { return status; }
    public Group getGroup() { return group; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }
    public void setPayer(User payer) { this.payer = payer; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(Status status) { this.status = status; }
    public void setGroup(Group group) { this.group = group; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Settlement s = new Settlement();
        public Builder payer(User v) { s.payer = v; return this; }
        public Builder receiver(User v) { s.receiver = v; return this; }
        public Builder amount(BigDecimal v) { s.amount = v; return this; }
        public Builder status(Status v) { s.status = v; return this; }
        public Builder group(Group v) { s.group = v; return this; }
        public Settlement build() { return s; }
    }
}
