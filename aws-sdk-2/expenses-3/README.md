# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 3

Goal: Implement lambda functions for retrieving mocked expenses

Steps:
1. [Implement a lambda for retrieving all expenses](#lambda-for-retrieving-all-expenses)
2. [Update handler to return expenses as JSON](#return-expenses-as-json)
3. [Implement a simple way of testing lambda](#simple-testing-approach)

### Lambda for retrieving all expenses
We first implement a handler that creates some mocked expenses, and allow retrieval of that.

* We add a `createMockedData()` method to `ExpenseAggregate` class.
* We implement `GetExpensesAsStringHandler` that has a static reference to an instance of `ExpenseAggregate`, and creates the mocked data via it. 
* We implement the `handleRequest` method to retrieve all the expenses and return that as a string 
* Deploy via `sls deploy`
* Invoke via `sls invoke -f get-expenses`

### Return expenses as JSON
The above handler returns the list of expenses as a string, which will use the `toString()` methods in `Expense` and `List` to create a string representation. This is not good for a proper API.

In `GetExpensesAsJsonHandler` we try and return the expenses directly as a collection. 

We update `serverless.yml` to define the new lambda function:
```yaml
functions:
  get-expenses:
    handler: com.example.expenses.lambda.GetExpensesAsStringHandler
  get-expenses-json:
    handler: com.example.expenses.lambda.GetExpensesAsJsonHandler
```

Now do `sls deploy` and `sls invoke -f get-expenses-json` to try it out.

#### How does this work?
The Java SDK that is part of the core lambda library we are using, provides automatic mapping of POJO (plain old java objects) to JSON, and vice versa. We will see how to map JSON requests to Java objects shortly. In `GetExpensesAsJsonHandler` the `List` of `Expense` objects gets mapped to JSON for us. It use the `Jackson` library, and we can use relevant annotations in our Java classes to tweak the way our classes are mapped to JSON.

An expense might be mapped to this JSON result:
```json
{
        "id": "e9098010-7b4e-4ad2-bc93-ccbbdfe1a306",
        "amount": 234,
        "date": {
            "year": 2022,
            "month": "MARCH",
            "monthValue": 3,
            "dayOfMonth": 21,
            "leapYear": false,
            "dayOfYear": 80,
            "dayOfWeek": "MONDAY",
            "era": "CE",
            "chronology": {
                "calendarType": "iso8601",
                "id": "ISO"
            }
        },
        "paidByPerson": {
            "email": "you@me.com"
        }
}
```

#### Dates and JSON
You will notice that the `LocalDate` field of our `Expense` objects are mapped to a very weird structure in JSON - this is the full properties structure for `LocalDate`, and not something that is compatible if we are to parse that JSON with other libraries or languages. 

Unfortunately it is not possible to tweak the way our objects are mapped to JSON using our current approach, i.e. where we leave everything to the AWS library. There are two options:

1. We change our handler class to implement `RequestStreamHandler`, which gets the input as an `InputStream` and have an `OutputStream` to write to, and we handle the deserialization and serialization ourselves. (out of scope of this example for now)
2. We implement our own response type class, where we have more control over serialization to JSON. (We will do this in [the next iteration](../expenses-4))


### Simple testing approach
It can become quite cumbersome to first have to deploy our lambda functions, and then invoke them directly in order to test.

However, we have something working in our favour: our lambda handlers do very little - it basically only translate the requests to methods to call on our domain logic (e.g. `ExpenseAggregate`), add some logging, and translate the domain result to a response. 

This means we can keep the bulk of our domain logic in "normal" unit tests, like we are doing in e.g. `ExpenseAggregateTest` and our model tests. You will notice that all the unit tests from [iteration 1](../expenses-1) are still working. You should keep these up to date as the domain classes are updated.

There are various frameworks out there that allows local mocked AWS services to be configured and run while you develop. [Serverless](https://serverless.com) also has some plugins that help with this, and AWS CDK and AWS SAM has tooling for this. However, these tools can be cumbersome to get working.

For basic testing we can get far without that, by simply mocking the runtime context and invoking our lambda handler methods directly.

* We first define a class `NullContext` that is a *Null object* implementation of the lambda `Context` interface.
* We then implement a unit test (`GetExpensesAsJsonHandlerTest`) that directly instantiates the lambda handler, and calls its `handleRequest` method with the `NullContext` instance and various request input values to test the scenarios we need to test.

```java
public class GetExpensesAsJsonHandlerTest {
    private GetExpensesAsJsonHandler testHandler;
    private Context testContext;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        testHandler = new GetExpensesAsJsonHandler();
        testContext = new NullContext();
    }

    @Test
    @DisplayName("Test that request to function returns list of expenses")
    void testGetExpenses(){
        List<Expense> result = testHandler.handleRequest(null, testContext);
        assertThat(result).asList().isNotEmpty();
        assertThat(result).asList().size().isEqualTo(3);
    }
}
```

In next iterations the testing will become more interesting, as we add different request event types for input, as well as start making use of other AWS services, like DynamoDB.