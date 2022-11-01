package com.wildsoft.cdk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awscdk.*;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.pipelines.*;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.organizations.OrganizationsClient;
import software.amazon.awssdk.services.organizations.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.organizations.model.Tag;
import software.constructs.Construct;

import java.util.*;

/**
 * This setup a AWS CodePipeline for building our core services stack
 */
public class CoreServicesCICDStack extends Stack {
    private static final Logger LOG = LogManager.getLogger(CoreServicesCICDStack.class);

    public CoreServicesCICDStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public CoreServicesCICDStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final SecretValue githubToken = SecretValue.secretsManager("GITHUB_TOKEN");
        final String repoName = this.getNode().tryGetContext("github_alias").toString() + "/" + this.getNode().tryGetContext("github_repo_name").toString();
        final CodePipeline pipeline = CodePipeline.Builder.create(this, "CoreServicesCodePipeline")
                .pipelineName("CoreServicesPipeline")
                .selfMutation(true)
                .crossAccountKeys(true)
                .dockerEnabledForSynth(true)
                .dockerEnabledForSelfMutation(true)
                .synth(CodeBuildStep.Builder.create("Synth")
                        .input(CodePipelineSource.gitHub(repoName, this.getNode().tryGetContext("github_repo_branch").toString(), GitHubSourceOptions.builder()
                                .authentication(githubToken).build()))
                        .rolePolicyStatements(List.of(
                                new PolicyStatement(PolicyStatementProps.builder()
                                        .actions(List.of(
                                                "organizations:ListAccounts",
                                                "organizations:ListTagsForResource"
                                        ))
                                        .resources(List.of("*"))
                                        .build())
                        ))
                        .installCommands(List.of(
                                "npm install -g aws-cdk",   // Commands to run before build
                                "cd ts_lambdas && npm install" //for our typescript based lambdas
                        ))
                        .commands(List.of(
                                "cd ..",
                                "mvn clean package",            //for our java based code
                                "cd infra",               // cd to infra CDK dir
                                "cdk synth"               // Synth command (always same)
                        ))
                        .primaryOutputDirectory("infra/cdk.out")
                        .env(Map.of("privileged", "true"))
                        .build())
                .build();

//        //output console url
//        CfnOutput.Builder.create(this, "PipelineConsoleUrl")
//                .value(String.format("https://%s.console.aws.amazon.com/codesuite/codepipeline/pipelines/%s/view?region=%s",
//                        Stack.of(this).getRegion(),
//                        codePipeline.getPipeline().getPipelineName(),
//                        Stack.of(this).getRegion()
//                        ))
//                .build();

        OrganizationsClient organizationsClient;
        if (System.getenv("CODEBUILD_BUILD_ID") == null){
            organizationsClient = OrganizationsClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("augmental-cicd"))
                .region(Region.US_EAST_1).build();
        } else {
            organizationsClient = OrganizationsClient.builder()
                    .region(Region.US_EAST_1).build();
        }
        List<StageDetails> stageDetails = new ArrayList<>();
        try {
            organizationsClient.listAccounts().accounts().forEach(account -> {
                List<Tag> tags = organizationsClient.listTagsForResource(ListTagsForResourceRequest.builder()
                                .resourceId(account.id())
                        .build()).tags();
                if (tags != null) {
                    Optional<Tag> accountTypeTag = tags.stream().filter(tag -> tag.key().equals("AccountType")).findFirst();
                    String accountType = null;
                    if (accountTypeTag.isPresent()){
                        accountType = accountTypeTag.get().value();
                    }
                    if (Objects.equals(accountType, "STAGE")){
                        String stageName = tags.stream().filter(tag -> tag.key().equals("StageName")).findFirst().orElse(Tag.builder().build()).value();
                        String stageOrder = tags.stream().filter(tag -> tag.key().equals("StageOrder")).findFirst().orElse(Tag.builder().build()).value();
                        stageDetails.add(new StageDetails(stageName, account.id(), stageOrder != null ? Integer.parseInt(stageOrder) : 0));
                    }
                }
            });
            stageDetails.sort(Comparator.comparingInt(StageDetails::getOrder));
            //Stage details names are: prod, or staging
            stageDetails.forEach(stageDetails1 -> {
                if (stageDetails1.getName().equalsIgnoreCase("prod")){
                    //for prod we want manual approval step
                    pipeline.addStage(new CoreServicesPipelineStage(this, stageDetails1.getName(), StageProps.builder()
                            .env(Environment.builder()
                                    .account(stageDetails1.getAccountId())
                                    .build())
                            .build()), AddStageOpts.builder()
                            .pre(List.of(
                                    new ManualApprovalStep("PromoteToProd")))
                            .build());
                } else {
                    pipeline.addStage(new CoreServicesPipelineStage(this, stageDetails1.getName(), StageProps.builder()
                            .env(Environment.builder()
                                    .account(stageDetails1.getAccountId())
                                    .build())
                            .build()));
                }
            });
        } catch (AwsServiceException e){
            LOG.error("AWS Error while attempting pipeline stack: "+e.getMessage(), e);
        }
    }

    private static class StageDetails {
        final String name;
        final String accountId;
        final int order;

        public StageDetails(String name, String accountId, int order) {
            this.name = name;
            this.accountId = accountId;
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public String getAccountId() {
            return accountId;
        }

        public int getOrder() {
            return order;
        }
    }

}
