# Using Java with AWS Serverless 

Java is indeed a first-class citizen of the AWS world. With the [Snapstart](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html) feature recently released by AWS (to address cold-start concerns), as well as support for *Java 17* runtime for AWS Lambdas, we can use a powerful language to implement our serverless functions. 

In this tutorial we will look at using the following technologies:

* Java (11) (Java 17 examples coming soon..)
* AWS Lambda, DynamoDB, API Gateway, SQS
* AWS [Java SDK v1](aws-sdk-1) and [Java SDK v2](aws-sdk-2) tutorials
* Deployment via [Serverless framework](https://serverless.com)
* Deployment via [AWS CDK](aws-cdk-java)

## AWS SDK for Java 1.x

This tutorial repo started by using AWS SDK for Java v1 - you can still get hold of it in the `aws-sdk-1` directory.

See the [AWS-SDK-1](aws-sdk-1) folder.

## AWS SDK for Java 2.x

This is a major rewrite of the 1.x SDK for Java. See [the AWS docs](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html) for more information about this version.

See the [AWS-SDK-2](aws-sdk-2) folder for a tutorial on using the 2.x sdk. 

## AWS CDK for Java

The section on using [Java for CDK](aws-cdk-java) is still very much *work in progress*, but should give some guidance already for using Java and CDK to deploy Lambda functions, API Gateway endpoints, and even setting up a AWS CodePipeline to trigger deployments from Github commits.
