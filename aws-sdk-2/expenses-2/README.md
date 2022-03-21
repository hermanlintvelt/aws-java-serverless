# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 2

Goal: Implement and deploy a basic AWS Lambda function.

Steps:
1. [Create `serverless.yml`](#serverlessyml---setting-up-cloud-resources)
2. [Create out `build.gradle`](#maven-vs-gradle---building-the-java-deployment-artifact) or `pom.xml` (maven) file
3. [Implement a basic Lambda handler](#lambda-handlers-introduction)
4. [Deploy and run the Lambda function](#how-to-deploy-and-run-our-lambda)

Also see the [Tips and Tricks](#tips--tricks) for additional info that might be handy in your adventure.

### Serverless.yml - setting up cloud resources
If you come across the name "The Serverless Framework" or you see a file `serverless.yml` being used in a project, then you must realise that the deployment tooling provided by [Serverless.com](https://serverless.com) is being used. This is just a technology that helps you to deploy your code and cloud resource configuration to a specific cloud provider (in our case AWS). There are other options as well:

* Use the AWS web console (please don't..)
* Use [AWS CDK (Cloud Development Kit)](https://aws.amazon.com/cdk/) (perhaps the topic of future tuts)
* Use [AWS SAM (Serverless Application Model)](https://aws.amazon.com/serverless/sam/)
* Use [AWS Cloudformation](https://aws.amazon.com/cloudformation/) directly (also not recommended)

[Serverless.com](https://serverless.com) provides easy to use tooling, with 3rd party plugins for extended functionality, that makes it very easy to setup various AWS resources, and get your code deployed as [AWS Lambda](https://aws.amazon.com/lambda/) functions.

It supports certain languages better than others, and unfortunately the getting-started templates for Java and Kotlin is quite outdated. 

The `serverless.yml` file is used by the Serverless framework to configure the necessary services on AWS, and know which code artifacts to upload and deploy as AWS Lambda functions.

* Refer to [serverless.yml](serverless.yml) for an example.

### Maven vs Gradle - building the Java deployment artifact
AWS Lambda support code from various programming languages. You can even compile a native binary and upload that as a custom container. 

For this tutorial, we deploy our code to AWS's lambda runtime for Java 11. In order to do this, we need to provide our compiles code, as well as any libraries (`.jar` files) that our code needs to run.

In order to implement AWS Lambda functions in Java, we need to include the following libraries as dependencies:

* `com.amazonaws:aws-lambda-java-core` - the core classes we need to implement a Lambda function in Java
* `com.amazonaws:aws-lambda-java-events` - a library containing various event classes that can be used to trigger functions,
* `com.amazonaws:aws-lambda-java-log4j2` - a library that makes it easier to log requests and responses from/to our lambda functions.

_Note: we do not need the *AWS SDK for Java V2* libraries yet when just implementing a Lambda function. We only need that when we make use of other AWS services from our lambda function code. We cover that in a next iteration._

_You can check for the latest versions at the [Maven Repository](https://mvnrepository.com). At the time of this README file update it was:
```
com.amazonaws:aws-lambda-java-core:1.2.1
com.amazonaws:aws-lambda-java-events:3.11.0
com.amazonaws:aws-lambda-java-log4j2:1.5.1
```

In our example we are using [Gradle](https://gradle.org) to manage dependencies, compile our code, and package the zip file that needs to be deployed.
The _gradle_ build creates [a zip file](#java-zip-file) that is referred to from the `serverless.yml` file, and uploaded to AWS as part of the deployment.

* Refer to the [build.gradle](build.gradle) file for an example.

_Note you can also use [Maven](https://maven.org) to build your Java code and package it in a zip file, as long as you end up with the content depicted in the [Java Zip File](#java-zip-file) section._

#### Java Zip file
Your `gradle` (or `mvn`) build must end up with a zip file that contains your compiled classes, as well as all libraries that you are dependent on (in a `libs` folder), as per the image below.
![](java-build-artifact.png)

### Lambda Handlers Introduction
An AWS Lambda function is basically a piece of code that can handle one or more events ("triggers), which will cause the code to be invoked. From a design point of view you want these functions to be simple and *do only one thing*. Thinks of it almost like a class with a `main` method, but instead of you running the class directly, AWS will run the code when you trigger it in some way.

Some ways in which you can cause this lambda function to run, are:
* invoke it directly (via a tool, or directly in AWS web console)
* link it to an HTTP endpoint via [AWS API Gateway](https://aws.amazon.com/api-gateway/), so that a request to that endpoint will trigger the lambda function
* link it to a specific [AWS S3](https://aws.amazon.com/s3/) Storage Bucket, so that e.g. a file uploaded there will cause the lambda function to be called
* link it to a specific [SQS](https://aws.amazon.com/sqs/) queue or [SNS](https://aws.amazon.com/sns/) topic, so that a message to that queue/topic will invoke the lambda function.
* and many others..

Let us start with a very simple implementation of a *Lambda Handler* (the term "lambda handler" is used for the code that is called directly when invoking a lambda function).

```java
package com.example.expenses.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class HelloHandler implements RequestHandler<String, String> {
    private static final Logger LOG = LogManager.getLogger(HelloHandler.class);

    @Override
    public String handleRequest(String request, Context context) {
        LOG.info("I received a request: "+request);
        LOG.info("My execution context is: "+context.toString());
        return request;
    }
}
```

We basically implement a class with a `handleRequest` method. This method gets called by the AWS Java runtime when a lambda function is invoked. To help define a standard interface, we need to implement the `RequestHandler` interface, which is part of the `aws-lambda-java-core` library. It makes use of generics to indicate the input type (which we set as `String`) and the response type (also `String` for our example) of the function. (We will see other options later.)

The `handleRequest` method also gets a `Context` parameter which contains information about the execution context for the lambda function. 

Our `HelloHandler` simply logs the string request it received, and also returns it as the response. 

#### Configuring our Lambda Function
AWS needs to know about `HelloHandler`, and since we are using _Serverless_ for our deployments, we need to define it in the `serverless.yml` file:
```yaml
functions:
  hello:
    handler: com.example.expenses.lambda.HelloHandler
```

This defines a lambda function with the name `expense-service-development-hello` (_service-stage-functionName_) which, when invoked, will call the `handleRequest` method of an instance of the `HelloHandler` class with the input request. 

### How to deploy and run our lambda
We have not yet linked any events (or "triggers) to our lambda function, but we can invoke it directly to try it out. 

* First we deploy it:`sls deploy` (or `serverless deploy`)
* We can list deployed functions: `sls deploy list functions`
* We can invoke the function directly: `sls invoke -f expenses-service-development-hello -d "say something"`

Refer to [Logging Basics](#logging-basics) for how to view the output from the logging when functions are invoked. 

## Tips & Tricks

### Logging basics
TODO: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/logging-slf4j.html
_Note: you can also view the `LOG` output from running the function code in [AWS Cloudwatch](https://aws.amazon.com/cloudwatch/)._

### On Environments
TODO something on dev, vs staging, vs prod environments and how to manage it 

### Java Code examples TODO: move to later iteration
Various code examples exist for the various parts of the AWS SDK.
Refer to the [AWS Code Samples for Java (SDK V2)](https://docs.aws.amazon.com/code-samples/latest/catalog/code-catalog-javav2.html) doc.

### Lambda Layers
TODO