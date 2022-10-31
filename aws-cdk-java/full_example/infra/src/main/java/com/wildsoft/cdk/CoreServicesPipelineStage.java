package com.wildsoft.cdk;

import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class CoreServicesPipelineStage extends Stage {
    public CoreServicesPipelineStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CoreServicesPipelineStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        new CoreServicesStack(this, "CoreServicesStack", id);
    }
}
