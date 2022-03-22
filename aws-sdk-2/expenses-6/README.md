# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 6

Goal: Persist the expenses to DynamoDB and change Lambdas to use the persisted data

Steps:
1. [Persisting to DynamoDB](#persisting-to-dynamodb)
2. [Create the actual DynamoDB table](#create-the-actual-dynamodb-table)
3. [Configure the Lambda permissions](#configure-the-lambda-permissions)
4. [How to test locally](#how-to-test-locally)
5. [Update lambda handlers to use new persistence](#update-lambda-handlers-to-use-new-persistence)

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
In order to use the enhanced DynamoDB client, we need to define a basic java class that represents the data we want to store. This is done by adding some specific annotations to the class, and ensuring the types of the properties are understood by the dynamoDB client lib, and the class is called a _DynamoDBBean_. We already have an `Expense` domain object, and while it is possible to annotate `Expense` in this way, we do not want to mix our persistence logic and implementation with our domain logic, so we rather implement a separate class, `ExpenseRecord` (in the name of separation of concerns).

```java
@DynamoDbBean
public class ExpenseRecord {
    private String id; //not UUID, we will convert it
    private String paidByPersonEmail; //not Person - we only store the person's email address
    private BigDecimal amount;
    private LocalDate date; //Instant gives us easy way to ensure UTC time

    //...

    //The @DynamoDbPartitionKey annotation indicates this is the unique lookup key used for a record
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
    
    //...
}
```


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

Then we must setup the DynamoDB table reference, using the schema as defined by the properties of `ExpenseRecord` class.

```java
public class DynamoDBRepository implements DataRepository {
    //...
    private final DynamoDbTable<ExpenseRecord> expensesTable;

    public DynamoDBRepository() {
        expensesTable = DB_ENHANCED_CLIENT.table("expenses-" + System.getenv("STAGE"), TableSchema.fromBean(ExpenseRecord.class));
    }
}
```

Lastly we implement the `addExpense`, `findExpensesPaidBy` and `allExpenses` methods to use the DynamoDB client api to insert data and retrieve data. 
You can [read more about the various requests and queries in the AWS doc](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-enhanced.html).

### Create the actual DynamoDB table
The above provides us a way to use the DynamoDB table in code. However, at this point it does not actually exist yet.

A simple way of creating the table as part of the deployment is to define it in the `serverless.yml` file:

```yaml
resources:
  Resources:
    ExpensesDynamoDbTable:
      Type: 'AWS::DynamoDB::Table'
      DeletionPolicy: Retain
      Properties:
        TableName: expenses-${self:provider.stage}
        AttributeDefinitions:
          - AttributeName: id
            AttributeType: S
        KeySchema:
          - AttributeName: id
            KeyType: HASH
        ProvisionedThroughput:
          ReadCapacityUnits: 1
          WriteCapacityUnits: 1
```

The `resources` section in `serverless.yml` is where we define other AWS resources needed by our service, using [AWS Cloudformation](https://aws.amazon.com/cloudformation/) configuration. It will create a DynamoDB table with the name `expenses-development` (the _stage_ variable), with a main _hash_ or _partition_ `id` (this must match the property you set as `@DynamoDbPartitionKey` in `ExpenseRecord`). 

The `ProvisionedThroughput` indicates how many clients can read or write to the table at the same time. 

There are a number of other configuration options available, see [the Cloudformation docs](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-dynamodb-table.html) and also refer [to Serverless.com docs on DynamoDB](https://www.serverless.com/guides/dynamodb).

_Note: it is also possible to create the DynamoDB tables by code. Look at the `createTable` method in the DynamoDB client api._

### Configure the Lambda permissions
In order for a lambda function to be able to access a DynamoDB table, it needs the correct permissions. 
We can assign permissions in the `serverless.yml` file:

```yaml
provider:
  #...
  iamRoleStatements: #the permissions needed by the Lambda functions to access other AWS resources
    - Effect: Allow
      Action:
        - dynamodb:*
      Resource: "arn:aws:dynamodb:${self:provider.region}:*:table/expenses-${self:provider.stage}"
```

Normally we would avoid a `*` wildcard in permissions, and rather define the exact list of permissions. For this tut we are taking the shortcut and leaving the specific permissions to the reader to figure out.

### How to test locally
We now have two implementations of `DataRepository`: `InMemoryRepository` and `DynamoDBRepository`. 
How will our code know which one to use? 

When testing locally, to keep things simple, we do not want to use DynamoDB, but when running on AWS, we want to use DynamoDB.

As already mentioned: the `serverless.yml` file sets the `STAGE` environment variable to `development` for the runtime environments of our lambda functions. Later we can enhance this with options for `production` and other stages.

In `build.gradle` we can use the following code to set a value for the `STAGE` env var:
```groovy
test {
    environment "STAGE","testing"
    useJUnitPlatform()
}
```

We can check for the value of the `STAGE` environment variable in our code, and use that to determine what implementation of `DataRepository` to use.
If it does not equal `testing`, we use `DynamoDBRepository`, else `InMemoryRepository`.


### Update lambda handlers to use new persistence
Since we will do this check in various places, we encapsulate it in a class `DataRepositoryFactory`, and change all the places (including tests) that instantiated `InMemoryRepository` directly to rather use the `DataRepositoryFactory.getDataRepository()` method.

Also remember to take out the call to `expenseAggregate.createMockedData()` in `GetExpensesHandler`.

Then do:

* `./gradlew build`
* 'sls deploy'
* and send a POST request with new expense to `/expenses` as per earlier iteration.

Now when you do a GET to `/expenses` endpoint, you should actually get the expense(s) you created via the POST request.

You can review the data in [the AWS DynamoDB Console](https://af-south-1.console.aws.amazon.com/dynamodbv2/home?region=af-south-1#table?initialTagKey=&name=expenses-development&tab=overview).