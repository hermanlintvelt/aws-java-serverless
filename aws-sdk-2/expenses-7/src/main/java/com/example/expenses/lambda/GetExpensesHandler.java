package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.expenses.aggregate.ExpenseAggregate;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.example.expenses.model.Expense;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Retrieves all expenses
 */
public class GetExpensesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(GetExpensesHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    private static final ExpenseAggregate EXPENSE_AGGREGATE = new ExpenseAggregate();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Retrieving all expenses");
        List<Expense> expenses = EXPENSE_AGGREGATE.getAllExpenses();
        try {
            return new APIGatewayProxyResponseEvent().withStatusCode(200)
                    .withBody(OBJECT_MAPPER.writeValueAsString(expenses));
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500)
                    .withBody("Error in converting result to JSON");
        }
    }
}
