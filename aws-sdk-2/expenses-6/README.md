# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 6

Goal: Persist the expenses to DynamoDB and change Lambdas to use the persisted data

Steps:
1. [Persisting to DynamoDB](#persisting-to-dynamodb)
2. [Configure DynamoDB Tables](#configure-the-dynamodb-tables)
3. [How to test locally](#how-to-test-locally)
4. [Update lambda handlers to use new persistence](#update-lambda-handlers-to-use-new-persistence)

### Persisting to DynamoDB
[AWS DynamoDB] provides an easy to use, fast and scalable NoSQL database service. There are other managed persistence services provided by AWS, but DynamoDB is a good one to start out with, and very scalable at low cost. In this tutorial we use it for all our lambdas, but nothing prevents you from using different storage services for different lambda functions or groups of lambda functions, similar to how in micro-services architecture the various micro services has their own data stores.

We already have a `DataRepository` interface that abstracts our persistence API, and we have an `InMemoryRepository` that just stored the data in a collection in memory. 

You might have notices that if we create expenses via a POST request to `/expenses` and then retrieve all expenses via a GET to `/expenses`, we are not currently getting the same expenses. This is because currently each lambda handler instance has its own, separate, instance of `ExpenseAggregate`, which in turn has its own instances of `InMemoryRepository`. Also these different lambda handler instances are not even running in the same processes or even same virtual servers, so they are not sharing memory in any way. 

We need a shared storage service for our expenses, used by these lambda handlers. To do this, we will implement `DynamoDBRepository`.

#### AWS SDK Dependencies
Finally, we have a need to use the [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html) that was already mentioned a few times. 

Specifically, we are going to use the more recent "enhanced" DynamoDB client sdk. It provides easier ways to map our Java objects to DynamoDB items.
You can [read more about using the DynamoDB enhanced client sdk](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-enhanced.html).

There are two ways to include the dependencies needed to make use of the SDK:

* either include the all-inclusive uber-jar file (if you do not worry about size, or don't want to spend time figuring out the exact dependencies) by defining a dependency on `software.amazon.awssdk:bom` (latest version at time of this update is `2.17.152`, as per the `build.gradle file)
* or find the specific jar file you need, in our case the one for DynamoDB "Enhanced" client API: `software.amazon.awssdk:dynamodb-enhanced:2.17.152`

_Note: if you want to see a list of all the individual jar files, visit [the BOM maven listing](https://mvnrepository.com/artifact/software.amazon.awssdk/bom/2.17.152)._

#### DynamoDBBean for Expense
In order to use the enhanced DynamoDB client, we need to define a basic java class that represents the data we want to store. This is done by adding some specific annotations to the class, and ensuring the types of the properties are understood by the dynamoDB client lib, and the class is called a _DynamoDBBean_. We already have an `Expense` domain object, and while it is possible to annotate `Expense` in this way, we do not want to mix our persistence logic and implementation with our domain logic, so we rather implement a separate class, `ExpenseRecord` (in the name of separatio of concerns).



#### DynamoDBRepository
`DynamoDBRepository` implements our `DataRepository` interface by using the DynamoDB client library that is part of the AWS SDK for Java.

First we create an instance of `DynamoDBEnhancedClient` which we use to send requests to DynamoDB. We first need an instance of `DynamoDBClient`, which provides a more basic api, then create the enhanced client from it. We also create this instances as part of the static initialization for our lambda handler class, as that has some performance benefits (which is outside the scope of this tut).

```java
public class DynamoDBRepository implements DataRepository {
    private final static DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient.builder()
            .region(Region.AF_SOUTH_1).build();
    private final static DynamoDbEnhancedClient DB_ENHANCED_CLIENT = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(DYNAMO_DB_CLIENT).build();
    //...
}
```

TODO

### Configure the DynamoDB Tables
TODO: serverless.yml config for DynamoDB

### How to test locally
TODO: using MOCKED env var to determine instance of DataRepository to load 

### Update lambda handlers to use new persistence
TODO: update and try it out
TODO: show data in console?