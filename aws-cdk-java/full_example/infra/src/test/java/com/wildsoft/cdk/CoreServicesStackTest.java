package com.wildsoft.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class CoreServicesStackTest {

    @Test
    public void testStack() throws IOException {
        App app = new App();
        CoreServicesStack stack = new CoreServicesStack(app, "test", "test");

        Template template = Template.fromStack(stack);

        template.resourceCountIs("AWS::SNS::Topic", 4);
    }
}
