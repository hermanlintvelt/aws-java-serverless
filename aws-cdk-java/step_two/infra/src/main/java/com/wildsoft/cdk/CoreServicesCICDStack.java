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
 * This setup an AWS CodePipeline for building our core services stack
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
        final CodePipeline pipeline = CodePipeline.Builder.create(this, "CoreServicesCodePipeline-Staging")
                .pipelineName("CoreServicesPipeline-Staging")
                .selfMutation(true)
                .crossAccountKeys(true)
                .dockerEnabledForSynth(true)
                .dockerEnabledForSelfMutation(true)
                .synth(CodeBuildStep.Builder.create("Synth")
                        .input(CodePipelineSource.gitHub(repoName, this.getNode().tryGetContext("github_repo_branch").toString(), GitHubSourceOptions.builder()
                                .authentication(githubToken).build()))
//                        .rolePolicyStatements(List.of(
//                                new PolicyStatement(PolicyStatementProps.builder()
//                                        .actions(List.of(
//                                                "organizations:ListAccounts",
//                                                "organizations:ListTagsForResource"
//                                        ))
//                                        .resources(List.of("*"))
//                                        .build())
//                        ))
//TODO: For Typescript:
//                      .installCommands(List.of(
//                                "npm install -g aws-cdk",   // Commands to run before build
//                                "cd ts_lambdas && npm install" //for our typescript based lambdas
//                        ))
                        .commands(List.of(
                                "cd ..",
                                "mvn clean package",            //for our java based code
                                "cd infra",               // cd to infra CDK dir
                                "cdk synth"               // Synth command (always same)
                        ))
                        .primaryOutputDirectory("infra/cdk.out")
//TODO: For Typescript:
//                          .env(Map.of("privileged", "true"))
                        .build())
                .build();

        pipeline.addStage(new CoreServicesPipelineStage(this, "staging", StageProps.builder()
                .build()));

        //output console url
        CfnOutput.Builder.create(this, "PipelineConsoleUrl")
                .value(String.format("https://%s.console.aws.amazon.com/codesuite/codepipeline/pipelines/%s/view?region=%s",
                        Stack.of(this).getRegion(),
                        pipeline.getPipeline().getPipelineName(),
                        Stack.of(this).getRegion()
                        ))
                .build();

    }
}
