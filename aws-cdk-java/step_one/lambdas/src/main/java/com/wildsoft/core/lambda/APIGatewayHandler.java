package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.wildsoft.domain.GeneralStuff;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class APIGatewayHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(APIGatewayHandler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Some testing request received");

        return new APIGatewayProxyResponseEvent().withStatusCode(200)
                .withBody(GeneralStuff.doSomething());
    }
}
