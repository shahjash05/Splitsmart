package com.splitsmart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "group_memberships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
public class GroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long membershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    public GroupMembership() {}

    public Long getMembershipId() { return membershipId; }
    public User getUser() { return user; }
    public Group getGroup() { return group; }

    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }
    public void setUser(User user) { this.user = user; }
    public void setGroup(Group group) { this.group = group; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final GroupMembership m = new GroupMembership();
        public Builder user(User v) { m.user = v; return this; }
        public Builder group(Group v) { m.group = v; return this; }
        public GroupMembership build() { return m; }
    }
}
