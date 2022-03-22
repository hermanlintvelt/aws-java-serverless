package com.example.expenses.lambda.aws;

import com.example.expenses.aggregate.EventNotifier;
import com.example.expenses.lambda.json.FullyConfiguredMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SQSEventNotifier implements EventNotifier {
    private static final Logger LOG = LogManager.getLogger(SQSEventNotifier.class);
    private static final ObjectMapper OBJECT_MAPPER = new FullyConfiguredMapper();
    private final String queueUrl;
    private final SqsClient sqsClient;

    public SQSEventNotifier() {
        String queueName = "expenses-queue-"+System.getenv("STAGE");
        sqsClient = SqsClient.builder().region(Region.AF_SOUTH_1).build();
        queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
    }

    @Override
    public void notifyEvent(ExpenseAdded event) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(OBJECT_MAPPER.writeValueAsString(event))
                    .build());
        } catch (JsonProcessingException e) {
            LOG.error("Could not write ExpenseAdded event to queue "+queueUrl, e);
        }
    }
}
