package com.splitsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`groups`")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 150)
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMembership> memberships = new ArrayList<>();

    public Group() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<GroupMembership> getMemberships() { return memberships; }

    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setMemberships(List<GroupMembership> memberships) { this.memberships = memberships; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Group g = new Group();
        public Builder groupName(String v) { g.groupName = v; return this; }
        public Builder createdBy(User v) { g.createdBy = v; return this; }
        public Group build() { return g; }
    }
}
