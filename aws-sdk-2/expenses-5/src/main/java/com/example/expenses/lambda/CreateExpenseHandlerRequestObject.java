package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.expenses.aggregate.ExpenseAggregate;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.example.expenses.model.Expense;
import com.example.expenses.model.Person;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

/**
 * Lambda Handler that creates an expense, using <code>CreateExpenseRequest</code> to present the request object
 */
public class CreateExpenseHandlerRequestObject implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(CreateExpenseHandlerRequestObject.class);
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    private static final ExpenseAggregate EXPENSE_AGGREGATE = new ExpenseAggregate();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Creating an expense from request: "+request.toString());
        try {
            CreateExpenseRequest createExpenseRequest = OBJECT_MAPPER.readValue(request.getBody(), CreateExpenseRequest.class);

            Expense newExpense = EXPENSE_AGGREGATE.createExpense(
                    new Expense(BigDecimal.valueOf(createExpenseRequest.getAmount()),
                            new Person(createExpenseRequest.getEmail())));
            LOG.info("Created expense: "+newExpense);
            return new APIGatewayProxyResponseEvent().withStatusCode(200)
                    .withBody(OBJECT_MAPPER.writeValueAsString(newExpense));
        } catch (JsonProcessingException e) {
            LOG.error("Error parsing request body to create new expense", e);
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid Expense object in request body.");
        }
    }

    private static class CreateExpenseRequest {
        private final String email;
        private final Double amount;

        @JsonCreator
        public CreateExpenseRequest(
                @JsonProperty("email") String email,
                @JsonProperty("amount") Double amount) {
            this.email = email;
            this.amount = amount;
        }

        public String getEmail() {
            return email;
        }

        public Double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return "CreateExpenseRequest{" +
                    "email='" + email + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }
}
