package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.expenses.aggregate.ExpenseAggregate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieves all expenses
 *
 * Note: we use <code>Void</code> as input type - this indicates that we are not expecting to provide any request input when invoking this lambda (which will only work if we invoke the lambda directly)
 */
public class GetExpensesAsStringHandler implements RequestHandler<Void, String> {
    private static final Logger LOG = LogManager.getLogger(GetExpensesAsStringHandler.class);

    private static final ExpenseAggregate EXPENSE_AGGREGATE = new ExpenseAggregate();

    public GetExpensesAsStringHandler(){
        EXPENSE_AGGREGATE.createMockedData();
    }

    @Override
    public String handleRequest(Void request, Context context) {
        LOG.info("Retrieving all expenses");
        return EXPENSE_AGGREGATE.getAllExpenses().toString();
    }
}
