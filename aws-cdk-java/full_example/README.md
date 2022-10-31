# Java AWS Serverless Example
Example containing

* AWS Lambda Java code
* CDK (Java) code for AWS resources needed

## Outline

1. AWS Lambda
- getting going - basic example, dependencies etc
- http lambda
- SQS/SNS lamba
- calling other lambdas
- env vars?
- init tips & tricks for faster response times
- dynamoDB tips & tricks - dynamodbmapper v2

2. AWS CDK
- docs hack (js doc -> javadoc)
- get lambda deployed
- hook up api gw
- tip: how to deal with api key
- tip: deploying lambda in python/ts
- tip: permissions
- code pipelines?
- orgs-structure...?

## Steps
0. pre-deploy example of everything to try out (different endpoint?) in case it fails..

First simple example
1. create project structure
2. Create a simple java lambda
3. deploy it via CDK
4. call it from commmandline
Now add HTTP callable lambda
5. change lambda to handle AWSGW request and return response
6. create GW endpoint via CDK, and deploy lambda with permissions
7. call via postman
How to add api key?
8. create api key via ???
9. add it and userplan to apigw endpoint
10. update postman call
How to deal with SQS/SNS
11. show lambda
12. update cdk to create queue/topic and deploy lambda with permissions
13. update http lambda to put message on topic to test 
Calling other lambdas
14. show invoker way of doing it
15. CDK update to deploy new lambda to test
Response time (cold boot)
16. init in static
17. pre-init AWS resources
18. example to show performance differences?
Tips (mentions only):
- dynamoDB mapper
- ??? (static init??)
CDK: deploying other language lambdas
19. python example
20. typescript example
CICD
21. basic codepipeline example

## Build & Deploy Stack changes

### Testing DEV build from machine

**Note:** AWS CDK CLI & Docker is required on your machine to deploy the solution

- `mvn clean package` - build the solution
- `cd infra`
- `cdk synth --profile <your profile>` - synth the solution
- `cdk deploy --profile <your profile>` - deploy the solution


## Build & Deploy CopePipeline changes

TODO: cleanup 

Sometimes a change to the stack can break the codepipeline, or for major changes to the pipeline stack, it might be necessary to directly build & deploy it.

- *NB*: comment out the non-pipeline stacks in `CoreServicesCdkApp`
- delete cdk cache in `cdk.out` directory
- `mvn clean package -DskipTests` - build the solution
- `aws sso login --profile augmental-cicd` - aws profile login
- `cdk-sso-sync augmental-cicd` - sync login with cdk profile
- `cdk synth --profile augmental-cicd`
- `cd infra; cdk deploy --profile augmental-cicd` - deploy the solution (to DEV)
- *NB*: comment back in the non-pipeline stacks in `CoreServicesCdkApp`

## Structure

* `infra` - Deploys lambda with CDK
* `lambdas` - Contains Java lambda functions
* `layer` - Creates layer for use by the lambdas
* `ts_lambdas` - Contains Typescript lambda functions


## Useful resources:

Resources: https://github.com/kolomied/awesome-cdk
Intro: https://cdkworkshop.com/50-java/70-advanced-topics/100-pipelines/5000-test-actions.html

JavaCDK Stack example: https://github.com/xerris/poc-javacdk/blob/master/src/main/java/com/myorg/JavaCdkStack.java

JavaDocs for AWS CDK lib: https://docs.aws.amazon.com/cdk/api/v2/java/index.html?software/amazon/awscdk/services/codepipeline/actions/ManualApprovalAction.html
Timestream best practices: https://docs.aws.amazon.com/timestream/latest/developerguide/data-modeling.html
`
AWS CDK Activate workshop: https://catalog.us-east-1.prod.workshops.aws/v2/workshops/13304db2-f715-48bf-ada0-92e5c4eea945/
