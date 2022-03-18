# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 2

Goal: Implement a basic AWS Lambda function that returns mocked expense data.

Steps:
1. Create `serverless.yml`
2. `maven` vs `gradle` file
3. Implement Lambda handler
4. Implement a simple way of testing lambda

### Serverless.yml
TODO: quick intro

### Maven vs Gradle
TODO: all the same, as long as you end up with proper zip artifact (show contents?)
TODO: AWS Java SDK dependencies needed

#### BOM vs individual jars
TODO: for dependencies see: https://mvnrepository.com/artifact/software.amazon.awssdk/bom/2.17.146
* using BOM vs individual SDK libs

### Lambda Handler
TODO: short intro
TODO: something about events and mapping to Java POJOs approaches

### Simple testing approach
TODO: testing without local mocked services

### Logging basics
TODO: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/logging-slf4j.html