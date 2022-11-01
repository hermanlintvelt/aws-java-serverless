package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringHandler implements RequestHandler<String, String> {
    private static final Logger LOG = LogManager.getLogger(StringHandler.class);

    @Override
    public String handleRequest(String request, Context context) {
        LOG.info("Some request received");

        return "Greetings";
    }
}
