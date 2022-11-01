package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageHandlerTest {

    private MessageHandler testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        testHandler = new MessageHandler();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Test the message handler")
    void testGetExpenses(){
        String result = testHandler.handleRequest(new MessageHandler.Message("Hello"), testContext);
        assertThat(result).isEqualTo("Greetings");
    }
}