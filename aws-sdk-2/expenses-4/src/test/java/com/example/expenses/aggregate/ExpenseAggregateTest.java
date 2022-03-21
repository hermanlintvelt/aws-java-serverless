package com.example.expenses.aggregate;

import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ExpenseAggregateTest {
    private ExpenseAggregate expenseAggregate;

    @BeforeEach
    void setUp() {
        expenseAggregate = new ExpenseAggregate();
    }

    @Test
    @DisplayName("Creating test should result in expense existing in service")
    void createExpense() {
        Person me = new Person("me@me.com");
        Expense expense = new Expense(BigDecimal.valueOf(150.0), me);
        expenseAggregate.createExpense(expense);
        assertThat(expenseAggregate.getAllExpenses()).asList().contains(expense);
    }

    @Test
    @DisplayName("If two expenses are created via service, the service must found them for person")
    void findExpensesPaidBy() {
        Person me = new Person("me@me.com");
        Expense expense1 = new Expense(BigDecimal.valueOf(150.0), me);
        expenseAggregate.createExpense(expense1);
        Expense expense2 = new Expense(BigDecimal.valueOf(80.0), me);
        expenseAggregate.createExpense(expense2);

        List<Expense> foundExpenses = expenseAggregate.findExpensesPaidBy(me.getEmail());
        assertThat(foundExpenses).isNotNull();
        assertThat(foundExpenses).asList().isNotEmpty();
        assertThat(foundExpenses).asList().containsOnly(expense1, expense2);
    }

    @Test
    @DisplayName("Searching for expense by non-existent person should return empty list")
    void findNonExistentPersonExpense(){
        assertThat(expenseAggregate.findExpensesPaidBy("unknown@mail.com")).asList().isEmpty();
    }
}