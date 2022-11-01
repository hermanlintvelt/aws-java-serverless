# Java AWS Serverless - Step One

- Step One: Basic lambdas and getting them deployed
- *Step Two: Adding CodePipeline to avoid manual deployments*
- Step Three: Tips & Tricks

## Step Two

### Structure

* `infra` - Deploys lambda with CDK
* `lambdas` - Contains Java lambda functions
* `layer` - Creates layer for use by the lambdas

### CICD with CodePipeline

How to create a Code Pipeline for builds, triggered from Github

* [Grant access to Github repo](https://catalog.us-east-1.prod.workshops.aws/workshops/13304db2-f715-48bf-ada0-92e5c4eea945/en-US/040-cicd/10-setup-your-git-repository)
* Setup token in AWS Secrets Manager: `aws secretsmanager create-secret --name GITHUB_TOKEN --secret-string <YOUR_GITHUB_PERSONAL_ACCESS_TOKEN> --profile <YOUR_PROFILE>`
* Set github values in `infra/cdk.json`
* Define a CodePipeline linked to github (see `CodeServicesCICDStack`) 
* Temporarily change `CoreServicesCdkApp` to setup code pipeline.

### Managing AWS Resources and Permissions

* Show how to create S3 Bucket and assign permissions to lambdas
* Show how to access Secure Parameter Store

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
