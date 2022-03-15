package com.example.expenses.repository.memory;

import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import com.example.expenses.repository.DataRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryRepository implements DataRepository {
    private final Set<Person> people = new HashSet<>();
    private final Set<Expense> expenses = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Person> allPersons() {
        return people.stream().collect(Collectors.toUnmodifiableList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Person> findPerson(String email) {
        return people.stream()
                .filter(Person -> Person.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person addPerson(Person person) {
        Optional<Person> alreadyExists = findPerson(person.getEmail());
        if (alreadyExists.isPresent()) {
            return alreadyExists.get();
        }
        people.add(person);
        return person;
    }

    @Override
    public Expense addExpense(Expense expense) {
        expenses.add(expense);
        return expense;
    }

    @Override
    public List<Expense> findExpensesPaidBy(Person person) {
        return expenses.stream().filter(expense -> expense.getPaidByPerson().equals(person)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Expense> allExpenses() {
        return expenses.stream().collect(Collectors.toUnmodifiableList());
    }
}