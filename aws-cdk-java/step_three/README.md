# Java AWS Serverless - Step Three

- Step One: Basic lambdas and getting them deployed*
- Step Two: Adding CodePipeline to remove manual deployments
- *Step Three: Tips & Tricks*

## Domain-driven Design approach

* Group lambda functions according to domain aggregates into "services"

## Separation of Concerns

* Lambda handler code just a thin "translation" layer
* Use proper domain models, repositories etc design patterns
* See [AWS SDK 2 Example 3](/aws-sdk-2/expenses-3)

## Managing AWS Resources and Permissions

* Show how to create S3 Bucket and assign permissions to lambdas
* Show how to access Secure Parameter Store from Lambdas
* Show SNS Handler example
* See `CoreServicesStack`

## Static Initializations

* Initialize "heavy" objects in static code - better access to hardware and not counting for billed ms
* For some AWS SDK classes also do a request to properly initialize
* see [AWS SDK 2 Example 7](/aws-sdk-2/expenses-7)

## DynamoDB Enhanced Client

* use `DynamoDbEnhancedClient` (from ` software.amazon.awssdk.enhanced.dynamodb` package)
* see [AWS SDK 2 Example 7](/aws-sdk-2/expenses-7)

## Lambda Power Tools

* Enhanced logging and tracing via [Lambda Power Tools](https://awslabs.github.io/aws-lambda-powertools-java/)

## Use the Typescript docs

* The Java docs for CDK is just *horrible*. 
* Just look at [this gem](https://docs.aws.amazon.com/cdk/api/v2/java/index.html?software/amazon/awscdk/services/timestream/CfnTable.html)
* _Tip:_ look at the Typescript docs (sometimes better) then find equivalent Java class.

## Calling other lambdas

* See `TestHandler` and `MyInvokableHandler`

## Deploying python and typescript lambdas

* See `CoreServicesStack`


