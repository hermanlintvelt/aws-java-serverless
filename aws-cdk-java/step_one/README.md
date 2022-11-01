# Java AWS Serverless - Step One

First simple example
1. create project structure
2. Create a simple java lambda - no input, string output
3. Unit test it - find lambda
4. deploy it via CDK
5. call it from commandline
`aws lambda invoke --function-name CoreServicesDEVStack-developmentsimplehandlerD999D-ra3ExXakKWsH --payload '' response.txt`

JSON to POJO mapping for Lambdas
6. Introduce `Message` request class
7. Unittest and deploy
8. Test via commandline
`aws lambda invoke --function-name CoreServicesDEVStack-developmentmessagehandler6D5F-a9zqbgSRXoiQ --cli-binary-format raw-in-base64-out --payload '{"message": "Hello"}' response.txt`

Layer dependencies
9. util/domain classes in layer

Http simple example
10. Handler for API Gateway events
11. Unittest
12. Deploy endpoint via CDK
13. Call via `curl`


## Structure

* `infra` - Deploys lambda with CDK
* `lambdas` - Contains Java lambda functions
* `layer` - Creates layer for use by the lambdas

## Build & Deploy Stack changes

**Note:** AWS CDK CLI & Docker is required on your machine to deploy the solution

*First time*: run `cdk bootstrap --profile <your profile>`

- `mvn clean package` - build the solution
- `cd infra`
- `cdk synth --profile <your profile>` - synth the solution
- `cdk deploy --profile <your profile>` - deploy the solution

## Useful resources:

Resources: https://github.com/kolomied/awesome-cdk
Intro: https://cdkworkshop.com/50-java/70-advanced-topics/100-pipelines/5000-test-actions.html

JavaCDK Stack example: https://github.com/xerris/poc-javacdk/blob/master/src/main/java/com/myorg/JavaCdkStack.java

JavaDocs for AWS CDK lib: https://docs.aws.amazon.com/cdk/api/v2/java/index.html?software/amazon/awscdk/services/codepipeline/actions/ManualApprovalAction.html
`
AWS CDK Activate workshop: https://catalog.us-east-1.prod.workshops.aws/v2/workshops/13304db2-f715-48bf-ada0-92e5c4eea945/
