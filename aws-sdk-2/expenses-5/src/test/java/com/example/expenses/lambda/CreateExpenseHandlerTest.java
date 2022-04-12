package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.example.expenses.model.Expense;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class CreateExpenseHandlerTest {
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    private RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        //testHandler = new CreateExpenseHandlerStart();
//        testHandler = new CreateExpenseHandlerJsonNode();
        testHandler = new CreateExpenseHandlerRequestObject();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Sending a valid request to create an expense must create and return a valid expense")
    void testCreateValidExpenses(){
        APIGatewayProxyResponseEvent result = testHandler.handleRequest(new APIGatewayProxyRequestEvent().withBody("{\n" +
                "    \"email\": \"me@me.com\",\n" +
                "    \"amount\": 100.0\n" +
                " }"), testContext);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        System.out.println("RESPONSE: "+result.getBody());
        try {
            Expense expense = OBJECT_MAPPER.readValue(result.getBody(), Expense.class);
            assertThat(expense).isNotNull();
        } catch (JsonProcessingException e) {
            fail("did not expect json error");
        }
    }

    @Test
    @DisplayName("Invalid JSON request body should fail with 400 error")
    void testCreateInvalidExpenses(){
        APIGatewayProxyResponseEvent result = testHandler.handleRequest(new APIGatewayProxyRequestEvent().withBody("{\n" +
                "    \"amount: 100.0\n" +
                " }"), testContext);
        assertThat(result.getStatusCode()).isEqualTo(400);
    }

}
