package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateRoadmapsHandlerTest {

	private static final Logger LOG = Logger.getLogger(CreateRoadmapsHandlerTest.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CreateRoadmapsHandler subject;
    private Context testContext;

    @BeforeEach
    public void setUp() {
        subject = new CreateRoadmapsHandler();
        testContext = new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            // implement all methods of this interface and setup your test context.
            // For instance, the function name:
            @Override
            public String getFunctionName() {
                return "ExampleAwsLambda";
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        };
    }

    private static String converToJson(Object objectBody){
        try {
            return objectMapper.writeValueAsString(objectBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void handleTestHandler() {
        RoadmapItem testItem = new RoadmapItem(
                UUID.randomUUID(),
                "Test item",
                "A first test item",
                RoadmapItem.PriorityType.NOW,
                LocalDate.of(2019,07,06));
        RoadmapRequest requestEvent = new RoadmapRequest();
        requestEvent.setBody(converToJson(testItem));
        ApiGatewayResponse response = subject.handleRequest(requestEvent, testContext);
        assertEquals(200, response.getStatusCode());

        assertEquals(converToJson(testItem), response.getBody());
    }

}
