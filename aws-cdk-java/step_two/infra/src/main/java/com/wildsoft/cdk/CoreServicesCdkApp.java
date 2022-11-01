package com.wildsoft.cdk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PermissionsBoundary;

public final class CoreServicesCdkApp {
    private static final Logger LOG = LogManager.getLogger(CoreServicesCdkApp.class);

    public static void main(final String[] args) {
        App app = new App();

        if (System.getenv("CODEBUILD_BUILD_ID") != null) {
            LOG.info("Starting CDK App - CICD Stack");
            //running on CI/CD trigger
            final Stack pipelineStack = new CoreServicesCICDStack(app, "CoreServicesCICDStack");

            final String permissionBoundaryArn = Fn.importValue("CICDPipelinePermissionsBoundaryArn");
            IManagedPolicy policy = ManagedPolicy.fromManagedPolicyArn(pipelineStack, "CICDPipelinePermissionsBoundary", permissionBoundaryArn);
            PermissionsBoundary.of(pipelineStack).apply(policy);
        } else {
            LOG.info("Starting CDK App - DEV Stack");
            //running manual from DEV
            //comment out this option and if statement and deploy CICD directly if need to renew pipeline manually
            new CoreServicesStack(app, "CoreServicesDEVStack", "development");
        }

        //always keep this here, for direct deploy or via pipeline
        app.synth();
    }

}
