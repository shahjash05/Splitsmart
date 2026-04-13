package com.splitsmart.repository;

import com.splitsmart.model.Expense;
import com.splitsmart.model.ExpenseParticipation;
import com.splitsmart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpenseParticipationRepository extends JpaRepository<ExpenseParticipation, Long> {
    List<ExpenseParticipation> findByExpense(Expense expense);
    Optional<ExpenseParticipation> findByExpenseAndUser(Expense expense, User user);
    List<ExpenseParticipation> findByUser(User user);
}
