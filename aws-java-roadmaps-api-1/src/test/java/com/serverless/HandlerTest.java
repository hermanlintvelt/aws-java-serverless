package com.serverless;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class HandlerTest {

	private static final Logger LOG = Logger.getLogger(Handler.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GetKeysHandler subject;
    private Context testContext;
    private KeysRepository keysRepository;

    @Before
    public void setUp() {
        subject = new GetKeysHandler();
        keysRepository = new MockedKeysRespository();
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
}
