package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.example.expenses.model.Expense;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class GetExpensesHandlerTest {
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    private GetExpensesHandler testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        testHandler = new GetExpensesHandler();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Test that request to function returns list of expenses")
    void testGetExpenses(){
        APIGatewayProxyResponseEvent result = testHandler.handleRequest(new APIGatewayProxyRequestEvent(), testContext);
        assertThat(result.getStatusCode()).isEqualTo(200);
        try {
            List<Expense> expenses = OBJECT_MAPPER.readValue(result.getBody(), List.class);
            assertThat(expenses).asList().isNotEmpty();
            assertThat(expenses).asList().size().isGreaterThanOrEqualTo(3);
        } catch (JsonProcessingException e) {
            fail("did not expect json error");
        }
    }
}
