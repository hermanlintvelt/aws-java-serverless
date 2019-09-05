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

5. Test it : unit test for Lambda Handlers

_Tips:_
* Lots of examples: (https://github.com/serverless/examples)
* Other project templates: (https://github.com/serverless/serverless/tree/master/lib/plugins/create/templates)

## 3. Add API endpoint

TODO - add API GW trigger event
TODO - show various request parsing methods (pojo, event classes, stream)

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