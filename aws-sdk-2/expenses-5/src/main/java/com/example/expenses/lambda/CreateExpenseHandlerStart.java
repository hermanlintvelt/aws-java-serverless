package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.expenses.aggregate.ExpenseAggregate;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Lambda Handler that creates an expense
 */
public class CreateExpenseHandlerStart implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(CreateExpenseHandlerStart.class);
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    private static final ExpenseAggregate EXPENSE_AGGREGATE = new ExpenseAggregate();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Creating an expense from request: "+request.toString());
        return new APIGatewayProxyResponseEvent().withStatusCode(200);
    }
}
