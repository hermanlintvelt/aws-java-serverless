package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a simple implementation of a Lambda handler that takes a <code>String</code> as input request and returns a <code>String</code> as output (or response).
 * In the <code>serverless.yml</code> file it is declared in the `functions` section, and given a name.
 * Any event that triggers the lambda function will cause the <code>handleRequest</code> method to be called.
 */
public final class HelloHandler implements RequestHandler<String, String> {
    private static final Logger LOG = LogManager.getLogger(HelloHandler.class);

    @Override
    public String handleRequest(String request, Context context) {
        LOG.info("I received a request: "+request);
        LOG.info("My execution context is: "+context.toString());
        return request;
    }
}
