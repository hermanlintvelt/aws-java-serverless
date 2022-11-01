package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class StringHandlerTest {

    private StringHandler testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        testHandler = new StringHandler();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Test the string handler")
    void testGetExpenses(){
        String result = testHandler.handleRequest("Hello", testContext);
        assertThat(result).isEqualTo("Greetings");
    }
}