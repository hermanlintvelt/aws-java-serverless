# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 4

Goal: Add an AWS API Gateway endpoint to call the Lambda function and test that via Postman

Steps:
1. [Update our configuration to define an API Gateway endpoint](#defining-an-api-gateway-endpoint)
2. [Sending requests to the endpoint](#sending-requests-to-the-endpoint)
3. [Update our code to handle API Gateway requests and responses](#update-handler-with-improved-api-gateway-requests-and-responses)

### Defining an API Gateway endpoint
TODO: what is it
TODO: how to connect to handler

### Sending requests to the endpoint
TODO: how to postman (curl?)
TODO: try out our handler - what goes wrong?

### Update handler with improved API Gateway requests and responses
TODO: events lib and how to use
TODO: custom APIGatewayResponse

#### TODO: Dates and JSON and custom APIGatewayResponse..
You will notice that the `LocalDate` field of our `Expense` objects are mapped to a very weird structure in JSON - this is the full properties structure for `LocalDate`, and not something that is compatible if we are to parse that JSON with other libraries or languages. 

We need to standardize this. One way to do this is to define our own *Serializer* and *Deserializer* for `LocalDate`. 

First we define the necessary dependencies on the Jackson libraries in our `build.gradle` file:
```groovy
dependencies {
    implementation(
            "com.fasterxml.jackson.core:jackson-core:${jacksonVersion}",
            "com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}",
            "com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}",
    )
```
In `gradle.properties` the `jacksonVersion` is set to `2.13.2` (latest at time of this update).

Now we can implement:
* [LocalDateSerializer.java](src/main/java/com/example/expenses/lambda/json/LocalDateSerializer.java) - transforms an instance of `LocalDate` to a string representation
* [LocalDateDeserializer.java](src/main/java/com/example/expenses/lambda/json/LocalDateDeserializer.java) - transforms a string to an instance of `LocalDate`

The last step is to indicate in our `Expense` class that we need to use the above serializer and deserialize for the `date` field:

```java
public class Expense {
    //...
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate date;
    //...
}
```

If we now build, deploy and invoke our function, we get a more expected JSON representation back:
```json
TODO
```

### Alternative way to work with requests as POJOs
TODO: defining own request object (or only when we do create/find handlers?)

