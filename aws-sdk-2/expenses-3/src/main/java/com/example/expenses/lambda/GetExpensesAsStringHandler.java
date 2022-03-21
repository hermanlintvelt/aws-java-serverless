package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieves all expenses
 */
public class GetExpensesAsStringHandler implements RequestHandler<String, String> {
    private static final Logger LOG = LogManager.getLogger(GetExpensesAsStringHandler.class);

    @Override
    public String handleRequest(String request, Context context) {
        LOG.info("I received a request: "+request);
        LOG.info("My execution context is: "+context.toString());
        return request;
    }
}
