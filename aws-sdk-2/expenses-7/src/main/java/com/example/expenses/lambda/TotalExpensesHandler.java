package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.example.expenses.aggregate.EventNotifier;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TotalExpensesHandler implements RequestHandler<SQSEvent, String> {
    private static final Logger LOG = LogManager.getLogger(TotalExpensesHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        for (SQSEvent.SQSMessage message: sqsEvent.getRecords()){
            try {
                EventNotifier.ExpenseAdded event = OBJECT_MAPPER.readValue(message.getBody(), EventNotifier.ExpenseAdded.class);
                LOG.info("Received Expense event: "+event);
            } catch (JsonProcessingException e) {
                LOG.error("Error reading SQS message body", e);
            }
        }

        return "OK";
    }
}
