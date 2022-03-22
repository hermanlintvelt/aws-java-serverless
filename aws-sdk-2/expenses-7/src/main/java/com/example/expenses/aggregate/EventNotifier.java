package com.example.expenses.aggregate;

import com.example.expenses.model.Expense;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface EventNotifier {
    /**
     * Notify specified event
     * TODO: for now we only have one event - later we can make this a polymorphic hierarchy of events and ensure some nice way of processing that on listener side
     * @param event
     */
    void notifyEvent(ExpenseAdded event);

    class ExpenseAdded {
        private Expense expense;

        @JsonCreator
        public ExpenseAdded(
                @JsonProperty("expense") Expense expense) {
            this.expense = expense;
        }

        public Expense getExpense() {
            return expense;
        }

        @Override
        public String toString() {
            return "ExpenseAdded{" +
                    "expense=" + expense +
                    '}';
        }
    }
}
