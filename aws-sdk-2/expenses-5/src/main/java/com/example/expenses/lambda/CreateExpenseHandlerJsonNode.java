package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.expenses.aggregate.ExpenseAggregate;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

/**
 * Lambda Handler that creates an expense
 */
public class CreateExpenseHandlerJsonNode implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(CreateExpenseHandlerJsonNode.class);
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    private static final ExpenseAggregate EXPENSE_AGGREGATE = new ExpenseAggregate();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Creating an expense from request: "+request.toString());
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(request.getBody());
            String email = jsonNode.get("email").asText();
            BigDecimal amount = BigDecimal.valueOf(jsonNode.get("amount").asDouble());
            Expense newExpense = EXPENSE_AGGREGATE.createExpense(new Expense(amount, new Person(email)));
            LOG.info("Created expense: "+newExpense);
            return new APIGatewayProxyResponseEvent().withStatusCode(200)
                    .withBody(OBJECT_MAPPER.writeValueAsString(newExpense));
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing request body to create new expense", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid Expense object in request body.");
        }
    }
}
