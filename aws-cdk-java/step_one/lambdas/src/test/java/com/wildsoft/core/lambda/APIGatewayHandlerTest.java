package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class APIGatewayHandlerTest {

    private APIGatewayHandler testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        testHandler = new APIGatewayHandler();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Test the httpo handler")
    void testGetExpenses(){
        APIGatewayProxyResponseEvent result = testHandler.handleRequest(new APIGatewayProxyRequestEvent(), testContext);
        assertThat(result.getStatusCode()).isEqualTo(200);
    }
}