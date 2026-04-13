package com.splitsmart.repository;

import com.splitsmart.model.Settlement;
import com.splitsmart.model.Settlement.Status;
import com.splitsmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("SELECT s FROM Settlement s WHERE (s.payer = :user OR s.receiver = :user)")
    List<Settlement> findByUser(@Param("user") User user);

    @Query("SELECT s FROM Settlement s WHERE (s.payer = :user OR s.receiver = :user) AND s.status = :status")
    List<Settlement> findByUserAndStatus(@Param("user") User user, @Param("status") Status status);

    @Query("SELECT s FROM Settlement s WHERE ((s.payer = :u1 AND s.receiver = :u2) OR (s.payer = :u2 AND s.receiver = :u1)) AND s.status = :status")
    List<Settlement> findConfirmedBetween(@Param("u1") User u1, @Param("u2") User u2, @Param("status") Status status);
}
