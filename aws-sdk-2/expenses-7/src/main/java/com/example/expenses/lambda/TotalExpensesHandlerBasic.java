package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TotalExpensesHandlerBasic implements RequestHandler<SQSEvent, String> {
    private static final Logger LOG = LogManager.getLogger(TotalExpensesHandlerBasic.class);

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        for (SQSEvent.SQSMessage message: sqsEvent.getRecords()){
            LOG.info("Received SQS Message: "+message);
        }

        return "OK";
    }
}
