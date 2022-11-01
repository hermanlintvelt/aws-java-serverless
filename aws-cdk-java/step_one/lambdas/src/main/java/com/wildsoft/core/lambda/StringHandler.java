package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class StringHandler implements RequestHandler<String, String> {

    @Override
    public String handleRequest(String request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Some request received");

        return "Greetings";
    }
}
