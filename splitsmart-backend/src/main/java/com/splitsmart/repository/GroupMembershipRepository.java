package com.splitsmart.repository;

import com.splitsmart.model.Group;
import com.splitsmart.model.GroupMembership;
import com.splitsmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    List<GroupMembership> findByGroup(Group group);
    Optional<GroupMembership> findByUserAndGroup(User user, Group group);
    boolean existsByUserAndGroup(User user, Group group);
    void deleteByUserAndGroup(User user, Group group);
}
