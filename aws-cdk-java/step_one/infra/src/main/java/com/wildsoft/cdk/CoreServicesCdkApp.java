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

        LOG.info("Starting CDK App - DEV Stack");
        //running manual from DEV
        new CoreServicesStack(app, "CoreServicesDEVStack", "development");

        //always keep this here, for direct deploy or via pipeline
        app.synth();
    }

}
