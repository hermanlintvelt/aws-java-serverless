package com.example.expenses.repository;

import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;

import java.util.List;
import java.util.Optional;

/**
 * DataRepository:
 * I define the protocol -- the set of methods -- that other parts of the application can use to
 * persist instances of the domain model (Persons, Expenses).
 * <p>
 * My name, {@code DataRepository}, is intended to indicate the abstract idea that <em>this</em>
 * is where data lives, without binding to any specific implementation like memory, file or database.
 * <p>
 */
public interface DataRepository {

    /**
     * Add a new Person instance to the datastore. If the Person passed to this method already exists
     * in the repository, we'll return that instance and ignore the request.
     * If you ask for a {@literal null} Person, it will throw a NullPointerException.
     * @param person A non-null Person instance.
     * @return The Person instance you added or that already existed in the repository.
     */
    Person addPerson(Person person);

    /**
     * Find a Person instance with the given email address.
     * @param email Email address of the Person we want to find.
     * @return An Optional containing the Person if they exist in the repository, empty otherwise.
     */
    Optional<Person> findPerson( String email );

    /**
     * Answer with a List of all Person instances in the repository.
     * @return A set of Person instances, possible empty, but never {@literal null}.
     */
    List<Person> allPersons();

    /**
     * Add a new Expense instance to the repository.
     * @param expense A non-null Expense instance
     * @return The Expense instance you added
     */
    Expense addExpense(Expense expense);

    /**
     * Finds all the expenses paid for by specified person.
     * @param person the non-null Person instance that must match the Expense's paidByPerson field.
     * @return A set of Expense instances, possible empty, but never {@literal null}. Sorted ascending according to date.
     */
    List<Expense> findExpensesPaidBy(Person person);

    /**
     * Answer with a List of all Expense instances in the repository.
     * @return A set of Expense instances, possible empty, but never {@literal null}. Sorted ascending according to date.
     */
    List<Expense> allExpenses();

}