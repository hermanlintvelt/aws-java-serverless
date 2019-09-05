# Java AWS Lambda with Serverless.com Tutorial

In which we build a *Roadmaps API* using:

* Java (8)
* AWS Lambda, DynamoDB, API GW, SSM
* Serverless.com
  
This project aims to show how to deploy a basic API using AWS serverless services, and [Serverless Framework](https://serverless.com) to deploy it.

## Setup

### Installation Setup

In order to do this tutorial, you need to install a few things:

* Install node and npm
* Install the [Serverless Framework](https://serverless.com) installed with an AWS account set up.
* Install Java 8 JDK (OpenJDK is good)
* Install [Gradle](http://gradle.org)

### Configuration Setup

* Create AWS account
* Setup credentials for *Serverless* using [this link](https://serverless.com/framework/docs/providers/aws/guide/credentials/)

## 1. Create Basic Handler

In this step we want to use *Serverless Framework* to create a basic _Java_-based lambda handler and deploy it. 

See code in `aws-java-roadmaps-api-1` folder.

1. Create project with *Serverless*: `serverless create --template aws-java-gradle --name roadmaps-api -p aws-java-roadmaps-api`
2. Update Project info:
   1. `build.gradle` : `baseName = "roadmaps-api"`
   2. `build.gradle` : update gradle wrapper to `4.10.x` or `5.x`
   3. `serverless.yml` : `package: artifact` : `hello.zip` -> `roadmaps-api.zip`
   4. `serverless.yml` : `funcions` : `hello` -> `roadmaps-handler`
   5. `serverless.yml` : update *AWS* profile
3. Build it : `./gradlew build`
4. Deploy it : `sls deploy`
5. Run it : `sls invoke -f roadmaps-handler --stage development --logs --data '{"someKey":"someValue"}'`

*What is missing?*

5. Test it :
   1. Add *JUnit* to `build.gradle`
   2. unit tests for Lambda Handlers

_Tips:_
* Lots of examples: (https://github.com/serverless/examples)
* Other project templates: (https://github.com/serverless/serverless/tree/master/lib/plugins/create/templates)

## 2. Add API endpoint

Let us now define an HTTP endpoint that calls our Lambda function, so the rest of the world can make use of our awesome _Roadmaps_ service.
For this we will define an `http` trigger, which will result in an *AWS API Gateway* endpoint being created. This endpoint is set as a _Lambda Proxy_ with our handler as the code that gets executed when it is called. *API Gateway* will pass on the request, as well as other context.

See code in `aws-java-roadmaps-api-2` folder.

### 2.1 Add API GW trigger event

1. Edit `serverless.yml` to add an http event trigger:

```yaml
functions:
  roadmaps-handler:
    handler: com.serverless.Handler
    events:
      - http:
          path: hello
          method: get
          cors: true
```

TODO - show various request parsing methods (map, event classes, pojos)

## 3. Add More Business Logic

TODO - handlers for updates etc

## 4. [Optional] Using Swagger to define API

TODO

## 5. DynamoDB for Persistence

TODO - show different dynamoDB mapping approaches? or just DynamoDBMapper Also indexes

## 6. Securing Credentials

TODO - show SSM usage

## 7. Monitoring

TODO - Xray, Cloudwatch, etc (serverless params for tracing)
[optional] Datadog, others?

## 8. SQS and SNS

TODO

## 9. Lambda Layers

TODO