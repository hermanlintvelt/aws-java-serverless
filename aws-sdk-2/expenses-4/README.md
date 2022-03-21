# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 4

Goal: Add an AWS API Gateway endpoint to call the Lambda function and test that via Postman

Steps:
1. [Update our configuration to define an API Gateway endpoint](#defining-an-api-gateway-endpoint)
2. [Sending requests to the endpoint](#sending-requests-to-the-endpoint)
3. [Update our code to handle API Gateway requests and responses](#update-handler-with-improved-api-gateway-requests-and-responses)

### Defining an API Gateway endpoint
[AWS API Gateway](https://aws.amazon.com/api-gateway/) allows you to define one or more HTTP endpoints (i.e. web service endpoints, each with an associated URL, which can be called via GET, POST, etc requests). These endpoints can be configured to invoke specific lambda functions with whatever data is in the HTTP request. 

As an example, we can setup that a `GET` request to an endpoint with the path `/expenses` return all the expenses. 

To do this, we update our function definition in `serverless.yml`:
```yaml
functions:
  get-expenses:
    handler: com.example.expenses.lambda.GetExpensesAsJsonHandler
    timeout: 30
    events:
      - http:
          path: expenses
          method: get
          cors: true
```

Here we define that:

* an event of type `http`, with method `get` (i.e. a http `GET` request) to the path `/expenses` should invoke our `GetExpensesAsJsonHandler` handler.
* by default, when using http events to trigger a lambda function, a timeout of *30 seconds* is applied, meaning that if your lambda code is still busy running after 30 seconds, it will fail with a timeout error. _I recommend making this timeout explicit in the `serverless.yml` file by indicating it here in the config._
* The `cors: true` indicates that this endpoint should be callable across origin domains - the details on this is outside scope of this tutorial, but it is worth reading up on [CORS (Cross-Origin Resource Sharing)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS).

When you run `sls deploy`, you will notice the following additional output:
```
endpoints:
  GET - https://<some-unique-id>.execute-api.af-south-1.amazonaws.com/development/expenses
```

This indicates that a web service endpoint has been created on API Gateway, and calling that via http requests will trigger your lambda. 

_Note: it is possible to define authorization for your endpoints using various approaches. [You can read more on the Serverless.com website.](https://www.serverless.com/framework/docs/providers/aws/events/apigateway)_

### Sending requests to the endpoint
After deployment, you can call the endpoint. Since this is a simple unauthorized GET request, we can just paste the endpoint link above into a browser and see what happens.
You can also use `curl` or a tool like [Postman](https://www.postman.com).

```
curl https://<some-unique-id>.execute-api.af-south-1.amazonaws.com/development/expenses
```

But wait! Something is going wrong, we are getting this response:
```json
{"message": "Internal server error"}
```

When delving a bit deeper into our *API Gateway Logs* on *Cloudwatch* (this might need some config), we see `Execution failed due to configuration error: Malformed Lambda proxy response`. Our handler code is being called, but API Gateway is not happy with the response object..

We actually need to expect specific types of request events and responses, depending on what kind of event can trigger our lambda function. In the next section we see how to deal with API Gateway events.

### Update handler with improved API Gateway requests and responsesA
In an earlier iteration we included the dependency `com.amazonaws:aws-lambda-java-events`. This library actually contains Java classes that encapsulate the different types of events that can trigger lambda handlers. I.e. can be used as the input type class for the `RequestHandler` interface we implement. It also defines some response types.

The ones specific to API Gateway events are:
* `APIGatewayProxyRequestEvent` - represents an event send to as input to the `handleRequest` method if invoked via an API Gateway endpoint
* `APIGatewayProxyResponseEvent` - represents the expected response type from `handleRequest` method if invoked via an API Gateway endpoint

We implement a new `GetExpensesHandler` class (we can drop the `asJson` part now) as follows:

```java
public class GetExpensesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Retrieving all expenses");
        List<Expense> expenses = EXPENSE_AGGREGATE.getAllExpenses();
        try {
            return new APIGatewayProxyResponseEvent().withStatusCode(200)
                    .withBody(OBJECT_MAPPER.writeValueAsString(expenses));
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500)
                    .withBody("Error in converting result to JSON");
        }
    }
}
```

Note that `OBJECT_MAPPER` is an instance of `FullyConfiguredMapper` that we provide in the example project - it instantiates a Jackson ObjectMapper with the correct configuration to handle the serialization of `LocalDate` (and other `java.time` classes) correctly. By using our own `ObjectMapper` we can control the serialization of the response objects better.

#### Testing!
We also need to add a unit-test for our API Gateway friendly handler. See `GetExpensesHandlerTest` for some rough inspiration.