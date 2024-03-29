# Java AWS Lambda with Serverless.com Tutorial

In which we build a *Expense Service* using:

* Java (11)
* AWS Lambda, DynamoDB, API GateWay, SQS, using the [AWS SDK for Java v2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html) 
* [Serverless.com](https://serverless.com) for deployment

This project aims to show how to deploy a basic API using AWS serverless services, and [Serverless Framework](https://serverless.com) to deploy it.

## Using CDK
This tutorial still make use of [Serverless.com](https://serverless.com) for deployment, but you can view [the CDK tutorial](../aws-cdk-java/README.md) for information on using AWS CDK to even implement your infrastructure as code in Java.

## Setup

### Installation Setup

In order to do this tutorial, you need to install a few things:

* Install node and npm
* Install the [Serverless Framework](https://serverless.com) installed with an AWS account set up.
* Install Java 11 JDK (OpenJDK is good)
* Install [Gradle](http://gradle.org) (_You can use [Maven](https://maven.org) as well to build your Java artifact._)

### Configuration Setup

* Create and AWS account and do the necessary setup (you can follow the `Setting Up` steps in [the AWS SDK for Java 2.x Quick Start guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html))
* Now setup credentials for *Serverless* using [this great article on how to setup Serverless to work with AWS](https://serverless.com/framework/docs/providers/aws/guide/credentials/).

## The Project

A basic API for tracking expenses.

* We will start with an API that allows to retrieve, create, update and delete expenses.
* We will then create a second service that listens to these expense events, and keep a list of expense totals per person. 

### Iterations

1. [Define some domain classes with tests](expenses-1)
2. [Implement a basic AWS Lambda function](expenses-2)
3. [Implement lambda functions for retrieving expenses](expenses-3)
4. [Add an AWS API Gateway endpoint to call the Lambda function and test that via Postman](expenses-4)
5. [Create a new API endpoint with Lambda function for creating expenses](expenses-5)
6. [Persist the expenses to DynamoDB and change Lambdas to use the persisted data](expenses-6)
7. [Create a Lambda function that listens to expense events via AWS Simple Queue Service](expenses-7)
8. (Later) hook up API Gateway endpoint to expense totals lambda
