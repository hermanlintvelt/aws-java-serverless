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
        final LayerVersion layer = new LayerVersion(this, "CoreLambdaLayer", LayerVersionProps.builder()
                .code(Code.fromAsset("../layer/target/bundle"))
                .compatibleRuntimes(List.of(Runtime.JAVA_11))
                .build());

        //Topics and Lambdas to serve them - receiving events from GCP etc
        createTopicWithLambda("Core Events", "core-events", "SNSEventsHandler", layer);

        //upload service used for uploading of Journal entry attachments
        //Create REST API endpoint
        RestApi uploadApi = createUploadApi(stageName);

        //Create upload bucket
        final Bucket bucket = createUploadS3Bucket();

        //Create update service lambdas
        createUploadServiceLambdas(uploadApi, bucket);
    }

    @NotNull
    private RestApi createUploadApi(String stageName) {
        RestApi uploadApi = RestApi.Builder.create(this, "UploadApi")
                .restApiName("CoreUploadApi")
                .description("Upload API Http endpoint")
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

//        //Get random value for api key
//        String apiKeyValue = generateRandomString(20);
        //Create ApiKey
//        IApiKey apiKey = ApiKey.Builder.create(this, "UploadApiKey")
//                .apiKeyName("upload-apikey2-"+stageName)
//                .value(apiKeyValue)
//                .description("Api Key for Upload Service API")
//                .resources(List.of(uploadApi))
//                .build();

        //Import manually created key
        IApiKey apiKey = ApiKey.fromApiKeyId(this, "UploadApiKey", getApiKeyId(stageName));

        final UsagePlan usagePlan = UsagePlan.Builder.create(this, "UploadApiDefaultUsagePlan")
                .name("UploadApiDefaultUsagePlan")
                .description("Default usage plan for Upload API")
                .apiStages(List.of(UsagePlanPerApiStage.builder()
                        .api(uploadApi)
                        .stage(uploadApi.getDeploymentStage())
                        .build()))
                .throttle(ThrottleSettings.builder()
                        .burstLimit(1500)
                        .rateLimit(1000)
                        .build())
                .build();
        usagePlan.addApiKey(apiKey);

        return uploadApi;
    }

    //we need this to map to hardcoded api key IDs, as they are generated and not deterministic, sadly
    private String getApiKeyId(String stageName){
        switch (stageName) {
            case "staging" : return "staging-key-id";
            case "prod": return "prod-key-id";
            default: return "dev-key-id";
        }
    }

    private void createTopicWithLambda(String description, String topicName, String lambdaName, LayerVersion layer){
        final Topic topicEvents = Topic.Builder.create(this, topicName)
                .displayName(description)
                .build();
        final Function snsFunction = new Function(this, lambdaName, FunctionProps.builder()
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../lambdas/target/lambdas.jar"))
                .handler("com.wildsoft.core.lambda."+lambdaName)
                .layers(List.of(layer))
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .logRetention(RetentionDays.ONE_YEAR)
                .build());
        topicEvents.addSubscription(new LambdaSubscription(snsFunction));
        CfnOutput.Builder.create(this, description+" topic")
                .description("")
                .value("Topic ARN: " + topicEvents.getTopicArn())
                .build();
    }

    private String generateRandomString(int targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private void createUploadServiceLambdas(RestApi uploadApi, Bucket bucket) {
        //load signing user credentials from parameter store
        IStringParameter accessIdParam = StringParameter.fromStringParameterName(this, "LambdaSigningUserAccessId", "/lambda-s3-signing/access-key-id");
        IStringParameter regionParam = StringParameter.fromStringParameterName(this, "LambdaSigningUserRegion", "/lambda-s3-signing/region");
        IStringParameter secretParam = StringParameter.fromSecureStringParameterAttributes(this, "LambdaSigningUserSecret",
                SecureStringParameterAttributes.builder()
                        .parameterName("/lambda-s3-signing/secret-access-key")
                        .version(1)
                        .build());

        //Create RequestPresignedURL lambda
//        createTypeScriptLambda("s3-upload-presigned-url-handler", "../ts_lambdas/src/functions/requestPresignedURL/handler.ts", uploadApi, "requestPresignedURL", bucket, accessIdParam, secretParam, regionParam);
        createPythonLambda("s3-upload-presigned-url-handler", "requestPresignedURL.py", uploadApi, "requestPresignedURL", bucket, accessIdParam, secretParam, regionParam);
        //Create RequestDownloadURL lambda
        //createTypeScriptLambda("s3-upload-download-url-handler", "../ts_lambdas/src/functions/requestDownloadURL/handler.ts", uploadApi, "requestDownloadURL", bucket, accessIdParam, secretParam, regionParam);
        createPythonLambda("s3-upload-download-url-handler", "requestDownloadURL.py", uploadApi, "requestDownloadURL", bucket, accessIdParam, secretParam, regionParam);
        //Create DeleteS3File lambda
        //createTypeScriptLambda("s3-upload-delete-file-handler", "../ts_lambdas/src/functions/deleteS3File/handler.ts", uploadApi, "deleteS3File", bucket, accessIdParam, secretParam, regionParam);
        createPythonLambda("s3-upload-delete-file-handler", "deleteS3File.py", uploadApi, "deleteS3File", bucket, accessIdParam, secretParam, regionParam);
        //Create createMultipartUpload lambda
        //createTypeScriptLambda("s3-create-multipart-upload-lambda", "../ts_lambdas/src/functions/createMultipartUpload/handler.ts", uploadApi, "createMultipartUpload", bucket, accessIdParam, secretParam, regionParam);
        createPythonLambda("s3-create-multipart-upload-lambda", "createMultipartUpload.py", uploadApi, "createMultipartUpload", bucket, accessIdParam, secretParam, regionParam);
        //Create requestMultipartPresignedUrl lambda
        //createTypeScriptLambda("s3-request-multipart-presigned-url-lambda", "../ts_lambdas/src/functions/requestMultipartUploadURLs/handler.ts", uploadApi, "requestMultipartPresignedURL", bucket, accessIdParam, secretParam, regionParam);
        createPythonLambda("s3-request-multipart-presigned-url-lambda", "requestMultipartPresignedURL.py", uploadApi, "requestMultipartPresignedURL", bucket, accessIdParam, secretParam, regionParam);
        //Create completeMultipartUpload lambda
        //createTypeScriptLambda("s3-complete-multipart-upload-lambda", "../ts_lambdas/src/functions/completeMultipartUpload/handler.ts", uploadApi, "completeMultipartUpload", bucket, accessIdParam, secretParam, regionParam);
        createPythonLambda("s3-complete-multipart-upload-lambda", "completeMultipartUpload.py", uploadApi, "completeMultipartUpload", bucket, accessIdParam, secretParam, regionParam);
        //Abort Multipart Upload
        createPythonLambda("s3-abort-multipart-upload-lambda", "abortMultipartFileUpload.py", uploadApi, "abortMultipartUpload", bucket, accessIdParam, secretParam, regionParam);
    }

    private void createTypeScriptLambda(String functionName, String entryFilePath, RestApi uploadApi, String httpPath, Bucket bucket, IStringParameter accessIdParam, IStringParameter secretParam, IStringParameter regionParam){
        //create lambda
        NodejsFunction function = NodejsFunction.Builder.create(this, functionName)
                .runtime(Runtime.NODEJS_14_X)
                .handler("main")
                .entry(entryFilePath)
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .logRetention(RetentionDays.THREE_MONTHS)
                .environment(Map.of("AWS_NODEJS_CONNECTION_REUSE_ENABLED", "1",
                        "NODE_OPTIONS", "--enable-source-maps --stack-trace-limit=1000",
                        "S3_BUCKET_NAME", bucket.getBucketName(),
                        "S3_BUCKET_REGION", getRegion()))
                .build();

        //Create Http integration for function
        LambdaIntegration simpleIntegration = LambdaIntegration.Builder.create(function)
                .requestTemplates(Map.of("application/json", "{ \"statusCode\": \"200\" }")
                ).build();

        IResource attPathResource;
        if (uploadApi.getRoot().getResource("attachments") == null) {
            attPathResource = uploadApi.getRoot().addResource("attachments");
        } else {
            attPathResource = uploadApi.getRoot().getResource("attachments");
        }
        Resource httpResource = attPathResource.addResource(httpPath);
        Method postMethod = httpResource.addMethod("POST", simpleIntegration,
                MethodOptions.builder()
                        .apiKeyRequired(true)
                        .build());


        //Assign Read/Write permissions to lambda for bucket
        bucket.grantReadWrite(function);
        //Assign ability to change ACL of S3 objects
        bucket.grantPutAcl(function);

        //Assign access to SSM parameters
        accessIdParam.grantRead(function);
        secretParam.grantRead(function);
        regionParam.grantRead(function);

        //Output created resources
        String urlPrefix = uploadApi.getUrl().substring(0, uploadApi.getUrl().length()-1);
        CfnOutput.Builder.create(this, "Output-"+functionName)
                .description("")
                .value("Endpoint URL: "+urlPrefix + postMethod.getResource().getPath())
                .build();
    }

    private void createPythonLambda(String functionName, String entryFilePath, RestApi uploadApi, String httpPath, Bucket bucket, IStringParameter accessIdParam, IStringParameter secretParam, IStringParameter regionParam){
        //create lambda
        PythonFunction function = PythonFunction.Builder.create(this, functionName)
                .entry("../python_lambdas") // required
                .runtime(Runtime.PYTHON_3_8) // required
                .index(entryFilePath)
                .handler("handler")
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .logRetention(RetentionDays.THREE_MONTHS)
                .environment(Map.of(
                        "S3_BUCKET_NAME", bucket.getBucketName(),
                        "S3_BUCKET_REGION", getRegion()))
                .build();

        //Create Http integration for function
        LambdaIntegration simpleIntegration = LambdaIntegration.Builder.create(function)
                .requestTemplates(Map.of("application/json", "{ \"statusCode\": \"200\" }")
                ).build();

        IResource attPathResource;
        if (uploadApi.getRoot().getResource("attachments") == null) {
            attPathResource = uploadApi.getRoot().addResource("attachments");
        } else {
            attPathResource = uploadApi.getRoot().getResource("attachments");
        }
        Resource httpResource = attPathResource.addResource(httpPath);
        Method postMethod = httpResource.addMethod("POST", simpleIntegration,
                MethodOptions.builder()
                        .apiKeyRequired(true)
                        .build());

        //Assign Read/Write permissions to lambda for bucket
        bucket.grantReadWrite(function);
        //Assign ability to change ACL of S3 objects
        bucket.grantPutAcl(function);

        //Assign access to SSM parameters
        accessIdParam.grantRead(function);
        secretParam.grantRead(function);
        regionParam.grantRead(function);

        //Output created resources
        String urlPrefix = uploadApi.getUrl().substring(0, uploadApi.getUrl().length()-1);
        CfnOutput.Builder.create(this, "Output-"+functionName)
                .description("")
                .value("Endpoint URL: "+urlPrefix + postMethod.getResource().getPath())
                .build();
    }

    private Bucket createUploadS3Bucket(){
        final Bucket bucket = Bucket.Builder.create(this, "S3UploadBucket")
                .bucketName(getRegion()+"-tracto-s3-upload-"+stageName)
                .versioned(false)
                .publicReadAccess(false)
                .cors(List.of(CorsRule.builder()
                        .allowedHeaders(List.of("Content-Type","X-Amz-Date","Authorization","X-Api-Key","Origin","Accept","Accept-Encoding","Host","User-Agent"))
                        .allowedMethods(List.of(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE))
                        .allowedOrigins(List.of("*"))
                        .build()))
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        CfnOutput.Builder.create(this, "Output-S3UploadBucket")
                .description("Bucket name of upload bucket")
                .value("ARN: "+bucket.getBucketArn())
                .build();
        return bucket;
    }
}
