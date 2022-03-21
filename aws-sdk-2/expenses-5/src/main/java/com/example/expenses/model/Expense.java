package com.example.expenses.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Expense {
    private final UUID id;
    private final BigDecimal amount;
    private final LocalDate date;
    private final Person paidByPerson;

    @JsonCreator
    public Expense(
            @JsonProperty("id") UUID id,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("paidByPerson") Person paidByPerson) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.paidByPerson = paidByPerson;
    }

    public Expense(BigDecimal amount, Person paidByPerson) {
        this(UUID.randomUUID(), amount, LocalDate.now(), paidByPerson);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public Person getPaidByPerson() {
        return paidByPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id) && Objects.equals(amount, expense.amount) && Objects.equals(date, expense.date) && Objects.equals(paidByPerson, expense.paidByPerson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, date, paidByPerson);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", amount=" + amount +
                ", date=" + date +
                ", paidByPerson=" + paidByPerson +
                '}';
    }
}