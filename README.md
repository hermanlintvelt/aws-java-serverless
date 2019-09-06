# Java AWS Lambda with Serverless.com Tutorial

In which we build a *Roadmaps API* using:

* Java (8)
* AWS Lambda, DynamoDB, API GW, SSM
* Serverless.com
  
This project aims to show how to deploy a basic API using AWS serverless services, and [Serverless Framework](https://serverless.com) to deploy it.

## Setup

### Installation Setup

In order to do this tutorial, you need to install a few things:

* Install node and npm
* Install the [Serverless Framework](https://serverless.com) installed with an AWS account set up.
* Install Java 8 JDK (OpenJDK is good)
* Install [Gradle](http://gradle.org)

### Configuration Setup

* Create AWS account
* Setup credentials for *Serverless* using [this link](https://serverless.com/framework/docs/providers/aws/guide/credentials/)

## 1. Create Basic Handler

In this step we want to use *Serverless Framework* to create a basic _Java_-based lambda handler and deploy it. 

See code in `aws-java-roadmaps-api-1` folder.

1. Create project with *Serverless*: `serverless create --template aws-java-gradle --name roadmaps-api -p aws-java-roadmaps-api`
2. Update Project info:
   1. `build.gradle` : `baseName = "roadmaps-api"`
   2. `build.gradle` : update gradle wrapper to `4.10.x` or `5.x`
   3. `serverless.yml` : `package: artifact` : `hello.zip` -> `roadmaps-api.zip`
   4. `serverless.yml` : `funcions` : `hello` -> `roadmaps-handler`
   5. `serverless.yml` : update *AWS* profile
3. Build it : `./gradlew build`
4. Deploy it : `sls deploy`
5. Run it : `sls invoke -f roadmaps-handler --stage development --logs --data '{"someKey":"someValue"}'`

*What is missing?*

5. Test it :
   1. Add *JUnit* to `build.gradle`
   2. unit tests for Lambda Handlers

```java
package com.serverless;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HandlerTest {

	private static final Logger LOG = Logger.getLogger(Handler.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Handler subject;
    private Context testContext;

    @BeforeEach
    public void setUp() {
        subject = new Handler();
        testContext = new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            // implement all methods of this interface and setup your test context.
            // For instance, the function name:
            @Override
            public String getFunctionName() {
                return "ExampleAwsLambda";
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        };
    }

    private static String converToJson(Object objectBody){
        try {
            return objectMapper.writeValueAsString(objectBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void handleTestHandler() {
        Map<String,Object> input = new HashMap<>();
        input.put("testKey","test value");
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(converToJson(input));
        ApiGatewayResponse response = subject.handleRequest(requestEvent, testContext);
        assertEquals(200, response.getStatusCode());

        Response expectedResponse = new Response("Go Serverless v1.x! Your function executed successfully!", converToJson(input));
        assertEquals(converToJson(expectedResponse), response.getBody());
    }

}
```

_Tips:_
* Lots of examples: (https://github.com/serverless/examples)
* Other project templates: (https://github.com/serverless/serverless/tree/master/lib/plugins/create/templates)

## 2. Add API endpoint

Let us now define an HTTP endpoint that calls our Lambda function, so the rest of the world can make use of our awesome _Roadmaps_ service.
For this we will define an `http` trigger, which will result in an *AWS API Gateway* endpoint being created. This endpoint is set as a _Lambda Proxy_ with our handler as the code that gets executed when it is called. *API Gateway* will pass on the request, as well as other context.

See code in `aws-java-roadmaps-api-2` folder.

### 2.1 Add API GW trigger event

1. Edit `serverless.yml` to add an http event trigger:

```yaml
functions:
  roadmaps-handler:
    handler: com.serverless.Handler
    events:
      - http:
          path: hello
          method: get
          cors: true
```

### 2.2 Handling the request as a stream

Our example handler uses a basic Map to contain the request and context paramaters being passed on to it.
In some use cases it could be valuable do deal with the input as a stream.

1. Create a new class `com.serverless.StreamHandler`:

```java
package com.serverless;
import java.io.InputStream;
import java.io.OutputStream;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.Context; 

public class StreamHandler implements RequestStreamHandler{
    public void handler(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        int letter;
        while((letter = inputStream.read()) != -1)
        {
            outputStream.write(Character.toUpperCase(letter));
        }
    }
}
```

2. Add handler definition to `serverless.yml`:

```yaml
functions:
  stream-handler:
    handler: com.serverless.StreamHandler
```

3. Invoke handler directly (or add http event if you want to call it via http):
   _Remember to *build & deploy* first._

`sls invoke -f stream-handler --logs --data '{"someKey":"someValue"}'`

### 2.3 Structuring the request parsing

While it is great fun to deal directly with Maps or Streams, and figure out the request from that, it is probably more sustainable if we can work with better defined request classes. The AWS SDK defines a number of these classes in the `com.amazonaws.services.lambda.runtime.events` package, for each of the typical events that can trigger a lambda, e.g. SNSEvent, SQSEvent, DynamodbEvent, S3Event, etc.

For our http event handler, we can make use of `APIGatewayProxyRequestEvent`.

1. Add the lambda events jar to `build.gradle`:

```gradle
dependencies {
    compile (
            'com.amazonaws:aws-lambda-java-events:2.2.6',
    )
}
```

2. Update `com.serverless.Handler.java` to use `APIGatewayProxyRequestEvent`:

```java
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(Handler.class);

    @Override
    public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LOG.info("received API request: " + request);
        Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully!", request.getBody());
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(responseBody)
                .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .build();
    }
}
```

3. Update `Response` class to not take Map:

```java
public class Response {

    private final String message;
    private final String input;

    public Response(String message, String input) {
        this.message = message;
        this.input = input;
    }

    public String getMessage() {
        return this.message;
    }

    public String getInput() {
        return this.input;
    }
}
```

4. Update Unit test!

```java
    @Test
    void handleTestHandler() {
        Map<String,Object> input = new HashMap<>();
        input.put("testKey","test value");
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(converToJson(input));
        ApiGatewayResponse response = subject.handleRequest(requestEvent, testContext);
        assertEquals(200, response.getStatusCode());

        Response expectedResponse = new Response("Go Serverless v1.x! Your function executed successfully!", converToJson(input));
        assertEquals(converToJson(expectedResponse), response.getBody());
    }
```

But now are getting `null` back as input in the response?! Hmm, let us see why in the logs:
`sls logs -f roadmaps-handler`

5. And we change the HTTP event to accept a `POST` rather (cause we want to send a body with the request):

In `serverless.yml` we change `http.method` from `get` to `post`.

### 2.4 Using POJOs for requests

The `APIGatewayProxyRequestEvent` can be a bit overwhelming, and need repetitive coding to parse the body into objects, etc. 
We can actually define our own Java class for the request type. The AWS SDK deals with the conversion to POJOs.

See code in `aws-java-roadmaps-api-2b` folder.

1. Define our very own `RoadmapRequest` class:

```java
package com.serverless.handlers;

public class RoadmapRequest {
    private String resource;
    private String httpMethod;
    private String body;
    private RoadmapPathParameters pathParameters;

    public RoadmapRequest(){}

    public RoadmapRequest(String resource, String httpMethod, String body, RoadmapPathParameters pathParameters) {
        this.resource = resource;
        this.httpMethod = httpMethod;
        this.body = body;
        this.pathParameters = pathParameters;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public RoadmapPathParameters getPathParameters() {
        if (pathParameters == null) return  new RoadmapPathParameters();
        return pathParameters;
    }

    public void setPathParameters(RoadmapPathParameters pathParameters) {
        this.pathParameters = pathParameters;
    }
    
    public static class RoadmapPathParameters {
        private String roadmapId;

        public RoadmapPathParameters(){}

        public RoadmapPathParameters(String roadmapId) {
            this.roadmapId = roadmapId;
        }

        public String getRoadmapId() {
            return roadmapId;
        }

        public void setRoadmapId(String roadmapId) {
            this.roadmapId = roadmapId;
        }
    }
}
```

2. Define a new lambda handler, so we start with a more domain oriented structure:

```java
package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.serverless.Response;
import org.apache.log4j.Logger;

public class RoadmapsHandler implements RequestHandler<RoadmapRequest, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(RoadmapsHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(RoadmapRequest request, Context context) {
        LOG.info("received Roadmap API request: " + request);
        Response responseBody = new Response("Roadmap request managed.", request.getBody());
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(responseBody)
                .build();
    }
}
```

3. Update `serverless.yml` to use new handler:

```yaml
functions:
  roadmaps-handler:
    handler: com.serverless.handlers.RoadmapsHandler
    events:
      - http:
          path: hello
          method: post
          cors: true
```

4. Update unit tests, build, deploy and call.

## 3. Add More Business Logic

TODO - handlers for updates etc

## 4. [Optional] Using Swagger to define API

TODO

## 5. DynamoDB for Persistence

TODO - show different dynamoDB mapping approaches? or just DynamoDBMapper Also indexes

## 6. Securing Credentials

TODO - show SSM usage

## 7. Securing the API

TODO: cognito vs api key auth
TODO: RequestContext in request class to get auth creds

## 7. Monitoring

TODO - enable tracing, Xray, Cloudwatch, etc (serverless params for tracing)
[optional] Datadog, others?

## 8. SQS and SNS

TODO

## 9. Lambda Layers

TODO