package com.splitsmart.repository;

import com.splitsmart.model.Group;
import com.splitsmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g JOIN g.memberships m WHERE m.user = :user")
    List<Group> findGroupsByMember(@Param("user") User user);
}
