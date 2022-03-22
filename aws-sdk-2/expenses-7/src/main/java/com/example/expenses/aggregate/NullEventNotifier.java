package com.example.expenses.aggregate;

public class NullEventNotifier implements EventNotifier{
    @Override
    public void notifyEvent(ExpenseAdded event) {
        //do nothing
    }
}
