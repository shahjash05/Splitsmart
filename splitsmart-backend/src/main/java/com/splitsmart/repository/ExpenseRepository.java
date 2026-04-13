package com.splitsmart.repository;

import com.splitsmart.model.Expense;
import com.splitsmart.model.Group;
import com.splitsmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroup(Group group);

    List<Expense> findByGroupIsNullAndCreatedBy(User user);

    @Query("SELECT e FROM Expense e JOIN e.participations p WHERE p.user = :user")
    List<Expense> findByParticipant(@Param("user") User user);

    @Query("SELECT e FROM Expense e JOIN e.participations p " +
           "WHERE p.user = :user AND e.dateOfExpense BETWEEN :start AND :end")
    List<Expense> findByParticipantAndDateRange(
            @Param("user") User user,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
