# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 5

Goal: Create a new API endpoint with Lambda function for creating expenses

Steps:
1. [Implement a lambda handler for creating a new expense](#implement-lambda-handler-to-create-a-new-expense)
2. [Adding support for request parameters](#adding-support-for-request-parameters)
3. [Extending the request to parse the expense date](#extending-the-request-to-parse-the-expense-date)

### Implement lambda handler to create a new expense
In our previous iteration we just do a basic GET call, with no parameters in the request. 

In order to be able to create an expense, we actually need to provide enough information to create it. The minimum we need is the *email address* of a person who paid for the expense, and the *amount*, which will create the expense for today's date.

To do that, we can provide the following JSON body in an http request:
```json
{
  "email": "me@me.com",
  "amount": 100.0
}
```

First we need to implement a new handler `CreateExpenseHandler` to be able to read the body from the request. 

```java
public class CreateExpenseHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LogManager.getLogger(CreateExpenseHandler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("Creating an expense from request: "+request.toString());
        return new APIGatewayProxyResponseEvent().withStatusCode(200);
    }
}
```

We start by just logging the request to see better what it contains exactly (which we'll explore in *CloudWatch*).

In `serverless.yml` we map a `POST` http event to this lambda handler:
```yaml
  create-expense:
    handler: com.example.expenses.lambda.CreateExpenseHandlerStart
    timeout: 30
    events:
      - http:
          path: expenses
          method: post
          cors: true
```

It is also a good idea to implement a unit test for this handler - see `CreateExpenseHandlerTest`. This will still fail until we sorted out the request handling.

When we build, deploy and invoke our function (via a POST http request), or via `curl`:

```
curl --location --request POST 'https://<your-unique-code>.execute-api.af-south-1.amazonaws.com/development/expenses' \
--header 'Content-Type: application/json' \
--data-raw '{
  "email": "me@me.com",
  "amount": 100.0
}'
```

Now we can [access the logs in Cloudwatch](https://af-south-1.console.aws.amazon.com/cloudwatch/home?region=af-south-1#logsV2:log-groups/log-group/$252Faws$252Flambda$252Fexpenses-service-development-create-expense)

Since we are printing out the whole request object in the LOG output, we see a lot of information - this is everything that API Gateway sends to our lambda handler. Of particular interest are these fields:
```
 resource: /expenses,
 path: /expenses,
 httpMethod: POST,
 headers: {...}
 body: {
    "email": "me@me.com",
    "amount": 100.0
 }
```

We need to parse our request object to get hold of the body. We can also use fields like `resource`, `headers`, `httpMethod` etc if we want to determine the handler's behaviour based on those parameters (e.g. you could implement a handler that behaves differently based on httpMethod, or that checks the request to determine authorized identity, etc)

### Adding support for request parameters
Let us look at two approaches for reading the request body. 

#### Reading the request body
We can parse the request body as a string, but that is cumbersome. Better to use our Jackson ObjectMapper to convert the body to an object. 

We can convert the body to a `JsonNode` object, which we can then use to get hold of properties:
```java
//From CreateExpenseHandlerJsonNode
JsonNode jsonNode = OBJECT_MAPPER.readTree(request.getBody());
```

Or we can define a class that represents what we expect in the request body, and convert to that. First we define a Java class that represents what we expect in the requests body, and then we update the `handleRequest` method to use that instead of `JsonNode`:
```java
//From CreateExpenseHandlerRequestObject
public class CreateExpenseHandlerRequestObject .. {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        //...
        CreateExpenseRequest createExpenseRequest = OBJECT_MAPPER.readValue(request.getBody(), CreateExpenseRequest.class);
        //...
    }

    private static class CreateExpenseRequest {
        private final String email;
        private final Double amount;
    
        @JsonCreator
        public CreateExpenseRequest(
                @JsonProperty("email") String email,
                @JsonProperty("amount") Double amount) {
            this.email = email;
            this.amount = amount;
        }
    
        public String getEmail() {
            return email;
        }
    
        public Double getAmount() {
            return amount;
        }
    }
}
```

The second approach is more type safe, but does involve a bit more work up front. However, as soon as your request objects (those presented by the request bodies) entails more than a few properties, then it is always worth it to define your own request objects. These are typically different than the domain objects, as e.g. the request objects usually do not contain identity fields and default values.

#### From JSON to Java Objects
If you want to convert the JSON responses back to e.g. an `Expense` object, e.g. like in `CreateExpenseHandlerTest`, then you will need to add a few JSON annotations to your `Expense` and `Person` classes, else the object mapper will not be able to convert the response bodies to objects.

```java
public class Expense {
    //...
    @JsonCreator
    public Expense(
            @JsonProperty("id") UUID id,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("paidByPerson") Person paidByPerson) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.paidByPerson = paidByPerson;
    }
    //...
}
```

And also for `Person`:
```java
public class Person {
    //...
    @JsonCreator
    public Person(
            @JsonProperty("email") String email) {
        this.email = email;
    }
    //...
}
```

#### Deploy and invoke
As soon as we build and deploy, and then try the POST request again, we get back an expense JSON result as expected:
```json
{
    "id": "df74d574-4f06-4b08-83a5-488e352376b0",
    "amount": 100.0,
    "date": "2022-03-21",
    "paidByPerson": {
        "email": "me@me.com"
    }
}
```

### Extending the request to parse the expense date
We ignored a custom date for the expense being created, but our domain model allows for this. 

Try adding a date to the create request:
* either parse it using `JsonNode` or by adding a date to the `CreateExpenseRequest` class
* perhaps add a constructor to `Expense` that accepts an amount, person and date only
* update your code to accept this date, if it is there, and use it, else use today's date
* remember to add tests for create request with and without a date
