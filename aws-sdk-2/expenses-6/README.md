# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 6

Goal: Persist the expenses to DynamoDB and change Lambdas to use the persisted data

Steps:
1. [Persisting to DynamoDB](#persisting-to-dynamodb)
2. [How to test locally](#how-to-test-locally)
3. [Update lambda handlers to use new persistence](#update-lambda-handlers-to-use-new-persistence)

### Persisting to DynamoDB
[AWS DynamoDB] provides an easy to use, fast and scalable NoSQL database service. There are other managed persistence services provided by AWS, but DynamoDB is a good one to start out with, and very scalable at low cost. In this tutorial we use it for all our lambdas, but nothing prevents you from using different storage services for different lambda functions or groups of lambda functions, similar to how in micro-services architecture the various micro services has their own data stores.

We already have a `DataRepository` interface that abstracts our persistence API, and we have an `InMemoryRepository` that just stored the data in a collection in memory. 

You might have notices that if we create expenses via a POST request to `/expenses` and then retrieve all expenses via a GET to `/expenses`, we are not currently getting the same expenses. This is because currently each lambda handler instance has its own, separate, instance of `ExpenseAggregate`, which in turn has its own instances of `InMemoryRepository`. Also these different lambda handler instances are not even running in the same processes or even same virtual servers, so they are not sharing memory in any way. 

We need a shared storage service for our expenses, used by these lambda handlers. To do this, we will implement `DynamoDBRepository`.

#### AWS SDK Dependencies
Finally, we have a need to use the [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html) that was already mentioned a few times. 

There are two ways to include the dependencies needed to make use of the SDK:

* either include the all-inclusive uber-jar file (if you do not worry about size, or don't want to spend time figuring out the exact dependencies) by defining a dependency on `software.amazon.awssdk:bom` (latest version at time of this update is `2.17.152`, as per the `build.gradle file)
* or find the specific jar file you need, in our case the one for DynamoDB "Enhanced" client API: `software.amazon.awssdk:dynamodb-enhanced:2.17.152`

_Note: if you want to see a list of all the individual jar files, visit [the BOM maven listing](https://mvnrepository.com/artifact/software.amazon.awssdk/bom/2.17.152)._

#### DynamoDBRepository
We are going to use the more recent "enhanced" DynamoDB client sdk for our implementation. It provides easier ways to map our Java objects to DynamoDB items.

TODO

### How to test locally
TODO: using MOCKED env var to determine instance of DataRepository to load 

### Update lambda handlers to use new persistence
TODO: update and try it out
TODO: show data in console?