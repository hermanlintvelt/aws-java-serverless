package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SNSEventsHandler implements RequestHandler<SNSEvent, String> {
    private static final Logger LOG = LogManager.getLogger(TestHandler.class);

    @Override
    public String handleRequest(SNSEvent snsEvent, Context context) {
        LOG.debug("AWS SNS Event: " + snsEvent);

        for (SNSEvent.SNSRecord message : snsEvent.getRecords()) {
            LOG.info("Event message: "+message.getSNS().getMessage());
            //TODO: transform and write to timestream and DynamoDB
        }

        return "OK";
    }
}
