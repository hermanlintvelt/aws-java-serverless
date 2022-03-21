package com.example.expenses.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ExpenseTest {

    @Test
    @DisplayName("Two expenses are the same if their fields are the same")
    void sameExpense() {
        UUID id = UUID.randomUUID();
        Person me = new Person("me@me.com");
        Expense e1 = new Expense(id, BigDecimal.valueOf(100.0), LocalDate.of(2022, 3, 15), me);
        Expense e2 = new Expense(id, BigDecimal.valueOf(100.0), LocalDate.of(2022, 3, 15), me);
        assertThat(e1).isEqualTo(e2);
    }

}