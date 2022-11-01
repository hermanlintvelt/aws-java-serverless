package com.wildsoft.cdk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.nodejs.NodejsFunction;
import software.amazon.awscdk.services.lambda.python.alpha.PythonFunction;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.CorsRule;
import software.amazon.awscdk.services.s3.HttpMethods;
import software.amazon.awscdk.services.sns.subscriptions.LambdaSubscription;
import software.amazon.awscdk.services.ssm.IStringParameter;
import software.amazon.awscdk.services.ssm.SecureStringParameterAttributes;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;
import software.amazon.awscdk.services.sns.Topic;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class CoreServicesStack extends Stack {
    private static final Logger LOG = LogManager.getLogger(CoreServicesStack.class);

    final String stageName;

    public CoreServicesStack(final Construct parent, final String id, String stageName) {
        this(parent, id, null, stageName);
    }

    public CoreServicesStack(final Construct parent, final String id, final StackProps props, String stageName) {
        super(parent, id, props);
        this.stageName = stageName;

        //add Lambda layer
        final LayerVersion layer = new LayerVersion(this, stageName+"-CoreLambdaLayer", LayerVersionProps.builder()
                .code(Code.fromAsset("../layer/target/bundle"))
                .compatibleRuntimes(List.of(Runtime.JAVA_11))
                .build());

        //simple handlers
        createStringHandler(layer);
        createMessageHandler(layer);

        //upload service used for uploading of Journal entry attachments
        //Create REST API endpoint
        RestApi uploadApi = createUploadApi(stageName);

        //api gateway handler
        createAPIGWHandler(uploadApi, layer);
    }

    @NotNull
    private RestApi createUploadApi(String stageName) {
        RestApi uploadApi = RestApi.Builder.create(this, stageName+"-UploadApi")
                .restApiName(stageName+"CoreUploadApi")
                .description(stageName+" Upload API Http endpoint")
                .deployOptions(StageOptions.builder()
                        .stageName(stageName)
                        .tracingEnabled(true)
                        .loggingLevel(MethodLoggingLevel.INFO)
                        .build())
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowHeaders(List.of("Content-Type","X-Amz-Date","Authorization","X-Api-Key","Origin","Accept","Accept-Encoding","Host","User-Agent"))
                        .allowMethods(List.of("GET","POST","OPTIONS","PUT","DELETE"))
                        .allowCredentials(true)
                        .allowOrigins(List.of("*"))
                        .build())
                .build();

//        //Import manually created key
//        IApiKey apiKey = ApiKey.fromApiKeyId(this, stageName+"-UploadApiKey", getApiKeyId(stageName));
//
//        final UsagePlan usagePlan = UsagePlan.Builder.create(this, stageName+"UploadApiDefaultUsagePlan")
//                .name(stageName+"UploadApiDefaultUsagePlan")
//                .description(stageName+" Default usage plan for Upload API")
//                .apiStages(List.of(UsagePlanPerApiStage.builder()
//                        .api(uploadApi)
//                        .stage(uploadApi.getDeploymentStage())
//                        .build()))
//                .throttle(ThrottleSettings.builder()
//                        .burstLimit(1500)
//                        .rateLimit(1000)
//                        .build())
//                .build();
//        usagePlan.addApiKey(apiKey);

        return uploadApi;
    }

    private void createStringHandler(LayerVersion layer){
        final Function simpleFunction = new Function(this, stageName+"-simple-handler", FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../lambdas/target/lambdas.jar"))
                .handler("com.wildsoft.core.lambda.StringHandler")
                .layers(List.of(layer))
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .logRetention(RetentionDays.ONE_YEAR)
                .build());

        CfnOutput.Builder.create(this, "String Handler: ")
                .description("")
                .value(simpleFunction.getFunctionName()+": "+simpleFunction.getFunctionArn())
                .build();
    }

    private void createMessageHandler(LayerVersion layer){
        final Function simpleFunction = new Function(this, stageName+"-message-handler", FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../lambdas/target/lambdas.jar"))
                .handler("com.wildsoft.core.lambda.MessageHandler")
                .layers(List.of(layer))
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .logRetention(RetentionDays.ONE_YEAR)
                .build());

        CfnOutput.Builder.create(this, "Message Handler: ")
                .description("")
                .value(simpleFunction.getFunctionName()+": "+simpleFunction.getFunctionArn())
                .build();
    }

//    private String generateRandomString(int targetStringLength) {
//        int leftLimit = 48; // numeral '0'
//        int rightLimit = 122; // letter 'z'
//        Random random = new Random();
//
//        return random.ints(leftLimit, rightLimit + 1)
//                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
//                .limit(targetStringLength)
//                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//                .toString();
//    }

    private void createAPIGWHandler(RestApi uploadApi, LayerVersion layer) {
        //create lambda
        final Function httpFunction = new Function(this, stageName+"-http-handler", FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../lambdas/target/lambdas.jar"))
                .handler("com.wildsoft.core.lambda.APIGatewayHandler")
                .layers(List.of(layer))
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .logRetention(RetentionDays.ONE_YEAR)
                .build());

        //Create Http integration for function
        LambdaIntegration simpleIntegration = LambdaIntegration.Builder.create(httpFunction)
                .requestTemplates(Map.of("application/json", "{ \"statusCode\": \"200\" }")
                ).build();

        IResource attPathResource;
        if (uploadApi.getRoot().getResource("mypath") == null) {
            attPathResource = uploadApi.getRoot().addResource("mypath");
        } else {
            attPathResource = uploadApi.getRoot().getResource("mypath");
        }
        Resource httpResource = attPathResource.addResource("myresource");
        Method postMethod = httpResource.addMethod("POST", simpleIntegration,
                MethodOptions.builder()
                        .apiKeyRequired(true)
                        .build());

//        //Assign Read/Write permissions to lambda for bucket
//        bucket.grantReadWrite(function);
//        //Assign ability to change ACL of S3 objects
//        bucket.grantPutAcl(function);
//
//        //Assign access to SSM parameters
//        accessIdParam.grantRead(function);
//        secretParam.grantRead(function);
//        regionParam.grantRead(function);

        //Output created resources
        String urlPrefix = uploadApi.getUrl().substring(0, uploadApi.getUrl().length()-1);
        CfnOutput.Builder.create(this, "Output-http-handler")
                .description("")
                .value("Endpoint URL: "+urlPrefix + postMethod.getResource().getPath())
                .build();
    }
}
