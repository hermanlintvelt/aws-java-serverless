package com.example.expenses.aggregate;

import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import com.example.expenses.repository.DataRepository;
import com.example.expenses.repository.DataRepositoryFactory;
import com.example.expenses.repository.memory.InMemoryRepository;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExpenseAggregate {
    private static final Logger LOG = LogManager.getLogger(ExpenseAggregate.class);
    private final DataRepository dataRepository = DataRepositoryFactory.getDataRepository();

//    public void createMockedData() {
//        Person me = new Person("me@me.com");
//        createExpense(new Expense(BigDecimal.valueOf(150.0), me));
//        createExpense(new Expense(BigDecimal.valueOf(234.0), me));
//        createExpense(new Expense(BigDecimal.valueOf(234.0),new Person("you@me.com")));
//    }

    /**
     * Create an event and ensure it is stored in the repository
     * @param expense
     * @return
     */
    public Expense createExpense(Expense expense){
        LOG.log(Level.INFO, "Creating an Expense for: "+expense);
        dataRepository.addPerson(expense.getPaidByPerson());
        return dataRepository.addExpense(expense);
    }

    /**
     * Finds expenses paid for by person with specified email address.
     * TODO: should improve error handling, ignoring that for now.
     * @param email
     * @return a list of expenses, or empty list if person not found
     */
    public List<Expense> findExpensesPaidBy(String email){
        LOG.log(Level.INFO, "Finding expenses paid for by person with email "+email);
        Optional<Person> personOptional = dataRepository.findPerson(email);
        if (personOptional.isPresent()){
            List<Expense> result = dataRepository.findExpensesPaidBy(personOptional.get());
            LOG.debug("Expenses result for "+email+" contains "+result.size()+" expenses.");
            return result;
        } else {
            LOG.warn("No Person found for email "+email);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieve all expenses currently stores in this service
     * @return
     */
    public List<Expense> getAllExpenses(){
        return dataRepository.allExpenses();
    }
}
