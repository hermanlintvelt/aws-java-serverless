# Java AWS Lambda with Serverless.com Tutorial 

## Expense Service Tutorial - Iteration 2

Goal: Implement a basic AWS Lambda function that returns mocked expense data.

Steps:
1. [Create `serverless.yml`](#serverlessyml---setting-up-cloud-resources)
2. [Create out `build.gradle`](#maven-vs-gradle---building-the-java-deployment-artifact) or `pom.xml` (maven) file
3. [Implement a basic Lambda handler](#lambda-handlers-introduction)
4. [Deploy and run the Lambda function](#how-to-deploy-and-run-our-lambda)
5. [Implement Expenses Lamnbda handler](#expenses-lambda-handlers)
6. [Implement a simple way of testing lambda](#simple-testing-approach)

Also see the [Tips and Tricks](#tips--tricks) for additional info that might be handy in your adventure.

### Serverless.yml - setting up cloud resources
If you come across the name "The Serverless Framework" or you see a file `serverless.yml` being used in a project, then you must realise that the deployment tooling provided by [Serverless.com](https://serverless.com) is being used. This is just a technology that helps you to deploy your code and cloud resource configuration to a specific cloud provider (in our case AWS). There are other options as well:

* Use the AWS web console (please don't..)
* Use [AWS CDK (Cloud Development Kit)](https://aws.amazon.com/cdk/) (perhaps the topic of future tuts)
* Use [AWS SAM (Serverless Application Model)](https://aws.amazon.com/serverless/sam/)
* Use [AWS Cloudformation](https://aws.amazon.com/cloudformation/) directly (also not recommended)

[Serverless.com](https://serverless.com) provides easy to use tooling, with 3rd party plugins for extended functionality, that makes it very easy to setup various AWS resources, and get your code deployed as [AWS Lambda](https://aws.amazon.com/lambda/) functions.

It supports certain languages better than others, and unfortunately the getting-started templates for Java and Kotlin is quite outdated. (So use the reference `serverless.yml` and `build.gradle` files from this tut rather than the ones from the serverless.com templates).

TODO: short example serverless.yml snippet

### Maven vs Gradle - building the Java deployment artifact
TODO: all the same, as long as you end up with proper zip artifact (show contents?)
TODO: AWS Lambda dedpencies needed

TODO: iter3+: to access AWS SDK for other AWS services for dependencies see: https://mvnrepository.com/artifact/software.amazon.awssdk/bom/2.17.146
* using BOM vs individual SDK libs

### Lambda Handlers Introduction
TODO: short intro to Lambdas
TODO: basic 'Hello World' handler 'HelloHandler'

### How to deploy and run our lambda
TODO: using sls to deploy
* Deploy:`sls deploy` (or `serverless deploy`)
* List deployed functions: `sls deploy list functions`
* Invoke function: `sls invoke -f expenses-service-development-hello`

### Expenses Lambda Handlers
TODO: handler to create Expense
TODO: something about events and mapping to Java POJOs approaches
TODO: handler to get all Expenses
TODO: if time, handler to find expense

### Simple testing approach
TODO: testing without local mocked services

## Tips & Tricks

### Logging basics
TODO: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/logging-slf4j.html

### On Environments
TODO something on dev, vs staging, vs prod environments and how to manage it 

### Java Code examples TODO: move to later iteration
Various code examples exist for the various parts of the AWS SDK.
Refer to the [AWS Code Samples for Java (SDK V2)](https://docs.aws.amazon.com/code-samples/latest/catalog/code-catalog-javav2.html) doc.