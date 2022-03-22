package com.example.expenses.repository;

import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DataRepositoryTest {
    private DataRepository dataRepository;

    @BeforeEach
    void setUp() {
        dataRepository = DataRepositoryFactory.getDataRepository();
    }

    @Test
    @DisplayName("A person not already added to repository should not be found")
    void unknownPersonNotFound() {
        assertThat(dataRepository.findPerson("unknown@mail.com")).isEmpty();
    }

    @Test
    @DisplayName("A new person added to repository should be found")
    void addPersonTest() {
        Person me = new Person("me@me.com");
        dataRepository.addPerson(me);
        assertThat(dataRepository.findPerson("me@me.com")).isNotEmpty().contains(me);
    }

    @Test
    @DisplayName("If multiple people are added to repository, then they should be retrieved")
    void allPersons() {
        Person me = new Person("me@me.com");
        dataRepository.addPerson(me);
        Person you = new Person("you@me.com");
        dataRepository.addPerson(you);

        List<Person> people = dataRepository.allPersons();
        assertThat(people).isNotNull();
        assertThat(people).asList().isNotEmpty();
        assertThat(people).asList().containsOnly(me, you);

    }

    @Test
    @DisplayName("A new expense added to repository should be found")
    void addExpenseTest() {
        Person me = new Person("me@me.com");
        Expense expense = new Expense(BigDecimal.valueOf(150.0), me);
        dataRepository.addExpense(expense);
        assertThat(dataRepository.allExpenses()).asList().contains(expense);
    }

    @Test
    @DisplayName("If multiple expenses are added for a person, it should be found")
    void addTwoExpensesForPerson() {
        Person me = new Person("me@me.com");
        Expense expense1 = new Expense(BigDecimal.valueOf(150.0), me);
        dataRepository.addExpense(expense1);
        Expense expense2 = new Expense(BigDecimal.valueOf(80.0), me);
        dataRepository.addExpense(expense2);

        List<Expense> foundExpenses = dataRepository.findExpensesPaidBy(me);
        assertThat(foundExpenses).isNotNull();
        assertThat(foundExpenses).asList().isNotEmpty();
        assertThat(foundExpenses).asList().contains(expense1, expense2);
    }

}