package com.example.expenses.repository.aws;

import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * This class is used by the enhanced DynamoDB client to determine the table schema, and know which properties are keys etc.
 */
@DynamoDbBean
public class ExpenseRecord {
    private String id; //not UUID, we will convert it
    private String paidByPersonEmail; //not Person - we only store the person's email address
    private BigDecimal amount;
    private LocalDate date; //Instant gives us easy way to ensure UTC time

    //We need the empty constructor for the DynamoDB client to do its magic.
    public ExpenseRecord() {
    }

    public ExpenseRecord(String id, String paidByPersonEmail, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.paidByPersonEmail = paidByPersonEmail;
        this.amount = amount;
        this.date = date;
    }

    //The @DynamoDbPartitionKey annotation indicates this is the unique lookup key used for a record
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaidByPersonEmail() {
        return paidByPersonEmail;
    }

    public void setPaidByPersonEmail(String paidByPersonEmail) {
        this.paidByPersonEmail = paidByPersonEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * This is a convenience method for constructing an instance of <code>ExpenseRecord</code> from an <code>Expense</code>
     * @param expense
     * @return
     */
    public static ExpenseRecord fromExpense(Expense expense){
        return new ExpenseRecord(
                expense.getId().toString(),
                expense.getPaidByPerson().getEmail(),
                expense.getAmount(),
                expense.getDate()
        );
    }

    /**
     * Maps this record to an instance of <code>Expense</code>
     * @return
     */
    public Expense asExpense(){
        return new Expense(
                UUID.fromString(this.getId()),
                this.getAmount(),
                this.getDate(),
                new Person(this.getPaidByPersonEmail())
        );
    }
}
