# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 7

Goal: Create a Lambda function that listens to expense events via AWS Simple Queue Service (SQS) and keep track of expense totals per person

Steps:
1. [Create SQS Topic](#create-sqs-topic)
2. [Listen to event from SQS](#listen-to-event-from-sqs)
3. [Send event to SQS](#send-event-to-sqs)
4. [Enhancing our listener](#enhancing-our-listener)

### Create SQS Topic
[AWS SQS (Simple Queue Service)](https://aws.amazon.com/sqs) is a messaging service (aka message broker service) that allows a software component to send messages (or events) to a queue, from where one or more other software components can retrieve the messages.

We will use it to asynchronously cause a lambda function to update a list of expense totals per person. Each time an expense is added, we just publish the fact to a specific queue via SQS, and we have another lambda function that will be notified whenever that queue receives a message.

We can create a queue manually in the AWS web console, or define it via the `resources` section in `serverless.yml` using a Cloudformation template. `serverless` will not create it automatically by hooking up an SQS event to a lambda (even though it does this for [SNS](https://www.serverless.com/framework/docs/providers/aws/events/sns)).

```yaml
resources:
  Resources:
    #...
    ExpensesQueue:
      Type: "AWS::SQS::Queue"
      Properties:
        QueueName: expenses-queue-${self:provider.stage}
        VisibilityTimeout: 60
        MessageRetentionPeriod: 120
        RedrivePolicy:
          deadLetterTargetArn:
            "Fn::GetAtt":
              - ExpensesDeadLetterQueue
              - Arn
          maxReceiveCount: 3
    ExpensesDeadLetterQueue:
      Type: "AWS::SQS::Queue"
      Properties:
        QueueName: expenses-DLQ-${self:provider.stage}
        MessageRetentionPeriod: 1209600 # 14 days in seconds
```

Next we link our new lambda handler with SQS events:
```yaml
funtions:
  #...
  total-expenses:
    handler: com.example.expenses.lambda.TotalExpensesHandler
    events:
      - sqs:
          arn:
            Fn::Join:
              - ':'
              - - arn
                - aws
                - sqs
                - Ref: AWS::Region
                - Ref: AWS::AccountId
                - expenses-queue-${self:provider.stage}
          batchSize: 1
```

The above will cause messages to the queue `expenses-queue-development` (our `provider.stage == development`) to invoke the `total-expenses` lambda function by calling the `handleRequest` method in `TotalExpensesHandler`. 
The `batchSize` parameter defines how many messages are sent per lambda function invocation to a specific instance of that lambda function. The default is `10`, but we make it `1` so we can show one message at a time for trying things out.

You can read more in [the serverless.com guide on SQS events](https://www.serverless.com/framework/docs/providers/aws/events/sqs).


We also need to set the permissions so our lambda functions can access the queue:

```yaml
provider:
  #..
  iamRoleStatements: 
    #..
    - Effect: "Allow"
      Action:
        - "sqs:*"
      Resource: "arn:aws:sqs:${self:provider.region}:*:expenses-queue-${self:provider.stage}"
```

### Listen to event from SQS
In order to trigger a lambda handler, it has to be able to handle an `SQSEvent` as input type. 

We first implement a very basic handler that will just log all messages it receives via the SQS queue it listens to (i.e. `expenses-queue-development` queue):

```java
public class TotalExpensesHandler implements RequestHandler<SQSEvent, String> {
    private static final Logger LOG = LogManager.getLogger(TotalExpensesHandler.class);

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        for (SQSEvent.SQSMessage message: sqsEvent.getRecords()){
            LOG.info("Received SQS Message: "+message);
        }

        return "OK";
    }
}
```

An `SQSEvent` contains one or more messages (sometimes a batch of messages can be sent at a time, depending on how the topic and subscription has been configured).
We simply print that out and return. 

Note that since no client code (on our side) directly called this handler, we won't see the "OK" response anywhere. If the `handleRequest` method exists with a `RuntimeException`, it will cause SQS to retry the sending of the message to another instance of the same lambda handler class, depending on the queue configuration. It is possible to configure a certain number of retry attempts, and alternative queues ("dead letter queues") where unhandled messages end up. 

### Send event to SQS
To see the `TotalExpensesHandler` respond to SQS messages, we actually need to send something to the queue. 

First we define an interface `EventNotifier` for sending interesting events, not linked directly to SQS:

```java
public interface EventNotifier {
    /**
     * Notify specified event
     * TODO: for now we only have one event - later we can make this a polymorphic hierarchy of events and ensure some nice way of processing that on listener side
     * @param event
     */
    void notifyEvent(ExpenseAdded event);

    class ExpenseAdded {
        private Expense expense;

        @JsonCreator
        public ExpenseAdded(
                @JsonProperty("expense") Expense expense) {
            this.expense = expense;
        }

        public Expense getExpense() {
            return expense;
        }
    }
}
```

We then implement a "null object pattern" version of this interface in `NullEventNotifier`, that just does nothing - we use this we run in `testing` stage.

`SQSEventNotifier` implements the interface to make use of the SQS client api. For this we need to include `software.amazon.awssdk:sqs:2.17.152` as a dependency in `build.gradle`.

```java
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
```

We need to instantiate the SQS client api, and also get hold of the queue URL. We do this once in the constructor. You can get more detail in the [AWS SQS Docs](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-sqs-messages.html).

In `notifyEvent` we make use of `sqsClient` to send the message. The body of the message consists of a JSON string version of `EventAdded` class. We can send a normal string here, or a byte array representing a serialized java class, or some other protocols, but to keep things easy and familiar we are sticking to JSON.

### Enhancing our listener
You can now enhance `TotalExpensesHandler` to keep a map of Person to total expenses in memory, or even store it to a totals DynamoDB table, for retrieval via another lambda handler and an API Gateway endpoint. 