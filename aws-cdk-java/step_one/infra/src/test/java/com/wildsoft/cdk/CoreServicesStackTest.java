package com.wildsoft.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CoreServicesStackTest {

    @Test
    public void testStack() throws IOException {
        App app = new App();
        CoreServicesStack stack = new CoreServicesStack(app, "test", "test");

        Template template = Template.fromStack(stack);

        template.resourceCountIs("AWS::Lambda::Function", 4);

        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Handler", "com.wildsoft.core.lambda.StringHandler",
                "Runtime", "java11"
        ));
        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Handler", "com.wildsoft.core.lambda.MessageHandler",
                "Runtime", "java11"
        ));
        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                "Handler", "com.wildsoft.core.lambda.APIGatewayHandler",
                "Runtime", "java11"
        ));
    }
}
