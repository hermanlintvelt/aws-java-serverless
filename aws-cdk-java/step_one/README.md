# Java AWS Serverless - Step One

- *Step One: Basic lambdas and getting them deployed*
- Step Two: Adding CodePipeline to remove manual deployments
- Step Three: Tips & Tricks

## Step One

### Structure

* `infra` - Deploys lambda with CDK
* `lambdas` - Contains Java lambda functions
* `layer` - Creates layer for use by the lambdas

### First simple example
1. create project structure
2. Create a simple java lambda - no input, string output
3. Unit test it - find lambda
4. deploy it via CDK
5. call it from commandline

`aws lambda invoke --function-name CoreServicesDEVStack-developmentsimplehandlerD999D-ra3ExXakKWsH --payload '' response.txt`

* view `cdk.json` and `pom.xml` to see how CDK finds the App to run for deployment

### JSON to POJO mapping for Lambdas
6. Introduce `Message` request class
7. Unittest and deploy
8. Test via commandline

`aws lambda invoke --function-name CoreServicesDEVStack-developmentmessagehandler6D5F-a9zqbgSRXoiQ --cli-binary-format raw-in-base64-out --payload '{"message": "Hello"}' response.txt`

### Layer dependencies
9. util/domain classes in layer

### Http simple example
10. Handler for API Gateway events
11. Unittest
12. Deploy endpoint via CDK
13. Call via `curl`

`curl https://qaxjj2p7kc.execute-api.eu-west-1.amazonaws.com/development/mypath/myresource`

* Check the *CloudWatch* logs
* Experiment with the event class
* Look at CORS configuration in API Gateway console

### Api Key

14. manuallly create api key
15. add it and userplan to apigw endpoint
16. deploy and test via `x-api-key` header

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
