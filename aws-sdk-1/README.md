# Java AWS Lambda with Serverless.com Tutorial

In which we build a *Roadmaps API* using:

* Java (11)
* AWS Lambda, DynamoDB, API GW, SSM
* Serverless.com
  
This project aims to show how to deploy a basic API using AWS serverless services, and [Serverless Framework](https://serverless.com) to deploy it.

_Slides_ There are [slides from a talk](https://www.slideshare.net/hermanlintvelt/going-serverless-with-java-a-real-life-story) that gives more context and specific tips. 

## Setup

### Installation Setup

In order to do this tutorial, you need to install a few things:

* Install node and npm
* Install the [Serverless Framework](https://serverless.com) installed with an AWS account set up.
* Install Java 11 JDK (OpenJDK is good)
* Install [Gradle](http://gradle.org)

### Configuration Setup

* Create AWS account and setup credentials for *Serverless* using [this great article on how to setup Serverless to work with AWS](https://serverless.com/framework/docs/providers/aws/guide/credentials/).

## 1. Create Basic Handler

In this step we want to use *Serverless Framework* to create a basic _Java_-based lambda handler and deploy it. 

See code in `aws-java-roadmaps-api-1` folder.

1. Create project with *Serverless*: `serverless create --template aws-java-gradle --name roadmaps-api -p aws-java-roadmaps-api`
2. Update Project info:
   1. `build.gradle` : `baseName = "roadmaps-api"`
   2. `build.gradle` : update gradle wrapper to `4.10.x` or `5.x`
   3. `serverless.yml` : `package: artifact` : `hello.zip` -> `roadmaps-api.zip`
   4. `serverless.yml` : `funcions` : `hello` -> `roadmaps-handler`
   5. `serverless.yml` : update *AWS* profile: `profile: default` (_or other profile name used_)
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

We played with different approached to handling request, and getting a basic Lambda deployed that is triggered from a HTTP API request. Now we want to think about what we actually want to build.

### The Domain

The idea is a basic *Roadmaps Service*, which allows a user to create, update, delete and retrieve roadmap items. 

We are keeping things simple to focus on the tech, so just have one roadmap (implied) with the ability to manage the roadmap items via an HTTP API.

A *Roadmap Item* needs the following information:

* A unique identifier 
* A short name
* A longer description
* A priority indicator (if it is one of _NOW_, _NEXT_ or _LATER_)
* A milestone date (optional - only used for real committed milestones)

See code in `aws-java-roadmaps-api-3` folder.

### Implement the domain classes

1. Create the class `com.serverless.domain.RoadmapItem`:

```java
package com.serverless.domain;

import java.time.LocalDate;
import java.util.UUID;

public class RoadmapItem {
    public enum PriorityType {
        NOW, NEXT, LATER
    }

    private UUID roadmapItemId;
    private String name;
    private String description;
    private PriorityType priorityType;
    private LocalDate milestoneDate;

    //TODO: generate constructors, get & set methods (or use Lombok)
}
```

However, ideally we also want to work with `RoadmapItem` instances on a request level, so for that we can update our `RoadmapRequest` class to handle the mapping from JSON.

2. Update `RoadmapRequest` to parse body's JSON

```java
..
    public Optional<RoadmapItem> getBodyAsRoadmapItem(){
        try {
            RoadmapItem item = objectMapper.readValue(this.body, RoadmapItem.class);
            return Optional.of(item);
        } catch (IOException e) {
            LOG.error("Error parsing body of request as RoadmapItem object: "+e.getMessage());
            return Optional.empty();
        }
    }

..
```

### Implement the Handlers

An easy approach is to have separate handlers for following (CRUD) use cases:

* Create RoadmapItem - handler that handles `POST` to endpoint with body defining the new roadmapItem
* Retrieve RoadmapItem(s) - handler that retrieves one or more items from a `GET` event
* Update RoadmapItem - handler that updates specified item from a `PUT` event
* Delete RoadmapItem - handler that deletes specified item from a `DELETE` event

#### CreateRoadmapsHandler

1. Define `CreateRoadmapsHandler` class:

```java
public class CreateRoadmapsHandler implements RequestHandler<RoadmapRequest, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(CreateRoadmapsHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(RoadmapRequest request, Context context) {
        LOG.info("received Create Roadmap API request: " + request);
        Optional<RoadmapItem> newItem = request.getBodyAsRoadmapItem();
        if (newItem.isPresent()){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(newItem.get())
                    .build();
        } else {
            return ApiGatewayResponse.builder()
                    .setStatusCode(204)
                    .setObjectBody(new Response("Error creating RoadmapItem","Could probably not parse the JSON"))
                    .build();

        }

    }
}
```

2. Update `serverless.yml` to define an http event to trigger this handler:

```yaml
functions:
  create-handler:
    handler: com.serverless.handlers.CreateRoadmapsHandler
    events:
      - http:
          path: roadmapitems
          method: post
          cors: true
```

3. We need that unit test as well, please..:

```java
package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateRoadmapsHandlerTest {

	private static final Logger LOG = Logger.getLogger(CreateRoadmapsHandlerTest.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CreateRoadmapsHandler subject;
    private Context testContext;

    @BeforeEach
    public void setUp() {
        subject = new CreateRoadmapsHandler();
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
        RoadmapItem testItem = new RoadmapItem(
                UUID.randomUUID(),
                "Test item",
                "A first test item",
                RoadmapItem.PriorityType.NOW,
                LocalDate.of(2019,07,06));
        RoadmapRequest requestEvent = new RoadmapRequest();
        requestEvent.setBody(converToJson(testItem));
        ApiGatewayResponse response = subject.handleRequest(requestEvent, testContext);
        assertEquals(200, response.getStatusCode());

        assertEquals(converToJson(testItem), response.getBody());
    }
}
```

*Now run the tests.* _So why does this fail?_

4. Add custom Json serialization for `LocalDate`:

```java
//LocalDateSerializer.java
package com.serverless.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateSerializer extends StdSerializer<LocalDate> {

    public LocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}

//LocalDateDeserializer.java
package com.serverless.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

    protected LocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return LocalDate.parse(parser.readValueAs(String.class));
    }
}
```

5. Update `RoadmapItem` to use these serializers:

```java
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate milestoneDate;
```

The unit tests should work fine now. 

6. Build, deploy and test the API `POST` request to `/roadmapitems` endpoint.

Those `nulls` in the response is not so nice. Easy to get rid of it with some annotations..

7. Update `RoadmapItem` to ignore _null_ values in JSON presentation.

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadmapItem {
    ..
}
```

#### GetRoadmapsHandler

Let us define a handler that handles the retrieval of all RoadmapItems, or of a specified item.

1. Implement `GetRoadmapsHandler`:

```java
package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class GetRoadmapsHandler implements RequestHandler<RoadmapRequest, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(GetRoadmapsHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(RoadmapRequest request, Context context) {
    LOG.info("received GET Roadmap API request: " + request);

    if (request.getResource() != null && request.getResource().equals("/roadmapitems/{roadmapItemId}")) {
        return handleSingleItemRequest(request);
    }

    return handleItemsRequest(request);
    }

    private ApiGatewayResponse handleSingleItemRequest(RoadmapRequest request) {
    String uuidStr = request.getPathParameters().getRoadmapId();
    UUID roadmapItemId = null;
    try {
        roadmapItemId = UUID.fromString(uuidStr);
    } catch (IllegalArgumentException e) {
        LOG.error("Unvalid UUID provided for roadmapItemId");
        return ApiGatewayResponse.builder()
                .setStatusCode(400)
                .setObjectBody(new Response("Error retrieving RoadmapItem","Unvalid UUID provided for roadmapItemId: "+uuidStr))
                .build();
    }


    RoadmapItem mockedItem = new RoadmapItem(
            roadmapItemId,
            "Mocked Item",
            "Mocked Item Description",
            RoadmapItem.PriorityType.NOW,
            LocalDate.now()
    );
    return ApiGatewayResponse.builder()
            .setStatusCode(200)
            .setObjectBody(mockedItem)
            .build();
    }

    private ApiGatewayResponse handleItemsRequest(RoadmapRequest request){
    List<RoadmapItem> mockedItems = new ArrayList<>();

    IntStream.range(1,10).forEach(i ->
            mockedItems.add(new RoadmapItem(
                    UUID.randomUUID(),
                    "Mocked Item "+i,
                    "Mocked Item Description "+i,
                    RoadmapItem.PriorityType.NOW,
                    LocalDate.now())));
    return ApiGatewayResponse.builder()
            .setStatusCode(200)
            .setObjectBody(mockedItems)
            .build();
    }
}
```

2. Update `serverless.yaml` to add this as function:

```yaml
functions:
  list-handler:
    handler: com.serverless.handlers.GetRoadmapsHandler
    events:
      - http:
          path: roadmapitems
          method: get
          cors: true
  get-handler:
    handler: com.serverless.handlers.GetRoadmapsHandler
    events:
      - http:
          path: roadmapitems/{roadmapItemId}
          method: get
          cors: true
```

3. Remember a unit test!

Now bulid and deploy. 
Try it out with `GET` to `/roadmapitems` as well as to e.g. `/roadmapitems/<itemid>`

_We will come back to handling *Update* and *Delete* of items later. First add some persistence.._

## 4. DynamoDB for Persistence

The AWS Java SDK has a nifty way of dealing with DynamoDB persistence called *DynamoDBMapper*. We will use that to add persistence to our service.

See code in `aws-java-roadmaps-api-4` folder.

### Adding Persistence code

We can jumple a whole bunch of context and annotations with our normal domain classses, e.g. `RoadmapItem`, but that will add complexity we do not want in our business/domain layer. Let us rather keep persistence stuff separate by create separate DTO classes. 

1. Add DynamoDB sdk lib dependency to `build.gradle`:
   
```gradle
dependencies {
    compile (
            ..
            'com.amazonaws:aws-lambda-java-events:2.2.6',
            ..
    )
}
```

2. Define `RoadmapItemDTO` to map our domain class to DynamoDB friendly entity:
   
```java
package com.serverless.persistence.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

import java.util.Date;

@DynamoDBTable(tableName="RoadmapItems")
public class RoadmapItemDTO {
    private static final Logger LOG = Logger.getLogger(RoadmapItemDTO.class);

    private String roadmapItemId;
    private String name;
    private String description;
    private RoadmapItem.PriorityType priorityType;
    private Date milestoneDate;

    @DynamoDBHashKey(attributeName="roadmapItemId")
    public String getRoadmapItemId() {
        return roadmapItemId;
    }

    public void setRoadmapItemId(String roadmapItemId) {
        this.roadmapItemId = roadmapItemId;
    }

    @DynamoDBAttribute(attributeName="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName="description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBTypeConverted(converter = PriorityTypeConverter.class)
    @DynamoDBAttribute(attributeName="priorityType")
    public RoadmapItem.PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(RoadmapItem.PriorityType priorityType) {
        this.priorityType = priorityType;
    }

    @DynamoDBAttribute(attributeName="milestoneDate")
    public Date getMilestoneDate() {
        return milestoneDate;
    }

    public void setMilestoneDate(Date milestoneDate) {
        this.milestoneDate = milestoneDate;
    }

    static public class PriorityTypeConverter implements DynamoDBTypeConverter<String, RoadmapItem.PriorityType> {

        @Override
        public String convert(RoadmapItem.PriorityType priorityType) {
            return priorityType.name();
        }

        @Override
        public RoadmapItem.PriorityType unconvert(String s) {
            return RoadmapItem.PriorityType.valueOf(s);
        }
    }

}
```

2. Add ways to convert between `RoadmapItem` and `RoadmapItemDTO`:

```java
//in `RoadmapItemDTO.java
    public static RoadmapItemDTO fromEvent(RoadmapItem item){
        RoadmapItemDTO dto = new RoadmapItemDTO();
        dto.setRoadmapItemId(item.getRoadmapItemId().toString());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPriorityType(item.getPriorityType());
        if (item.getMilestoneDate()!=null){
            dto.setMilestoneDate(DateUtils.asDateUTC(item.getMilestoneDate()));
        }
        return dto;
    }

    public RoadmapItem asRoadmapItem(){
        return new RoadmapItem(
                UUID.fromString(getRoadmapItemId()),
                getName(),
                getDescription(),
                getPriorityType(),
                getMilestoneDate()!=null?DateUtils.asLocalDateUTC(getMilestoneDate()):null
        );
    }
```

3. We need some helper classes to hook up the right tables, etc:

```java
package com.serverless.persistence.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

public class DynamoDBConfig {
    private static final Logger LOG = Logger.getLogger(DynamoDBConfig.class);

    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
        builder.setTableNameResolver(new CustomNameResolver());
        return builder.build();
    }

    private static class CustomNameResolver implements DynamoDBMapperConfig.TableNameResolver {
        public String getTableName(Class<?> clazz, DynamoDBMapperConfig config) {
            if (clazz.equals(RoadmapItem.class)){
                String tableName = System.getenv("ROADMAP_ITEMS_TABLE");
                LOG.info("RoadmapItems DBTable name configuration with name: "+tableName);
                return tableName;
            }
            else return "UnknownTable";
        }
    }
}
```

4. Now we create a `RoadmapItemsRepository` class to do the actual persistence work:

```java
package com.serverless.persistence.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoadmapItemsRepository {
    private static final Logger LOG = Logger.getLogger(RoadmapItemsRepository.class);

    private final DynamoDBMapper dbMapper;

    public RoadmapItemsRepository() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        dbMapper = new DynamoDBMapper(client, new DynamoDBConfig().dynamoDBMapperConfig());
    }

    public RoadmapItem createRoadmapItem(RoadmapItem newItem){
        if (newItem.getRoadmapItemId()==null){
            newItem.setRoadmapItemId(UUID.randomUUID());
        }
        RoadmapItemDTO dto = RoadmapItemDTO.fromItem(newItem);
        dbMapper.save(dto);
        LOG.info("Created new RoadmapItem in dynamoDB: "+newItem);

        return getRoadmapItem(newItem.getRoadmapItemId());
    }

    public RoadmapItem getRoadmapItem(UUID roadmapItemid){
        RoadmapItemDTO result = dbMapper.load(RoadmapItemDTO.class, roadmapItemid.toString());
        if (result == null) return null;
        return result.asRoadmapItem();
    }

    public List<RoadmapItem> getRoadmapItems() {
        return dbMapper.scan(RoadmapItemDTO.class, new DynamoDBScanExpression().withLimit(1000))
                .stream().map(RoadmapItemDTO::asRoadmapItem).collect(Collectors.toList());
    }

}
```

5. And we actually need to define the DynamoDB resources (tables and indexes) in `serverless.yml`, as well as the IAM permissions to access them:
   
```yaml
..
provider:
  ..
  environment:
    ROADMAP_ITEMS_TABLE: ${self:service}-${opt:stage, self:provider.stage}-roadmapitems
  iamRoleStatements:
    - Effect: Allow
      Action:
        - dynamodb:Query
        - dynamodb:Scan
        - dynamodb:GetItem
        - dynamodb:PutItem
        - dynamodb:UpdateItem
        - dynamodb:DeleteItem
      Resource: "arn:aws:dynamodb:${opt:region, self:provider.region}:*:table/${self:provider.environment.ROADMAP_ITEMS_TABLE}"
..
//at the end
resources:
  Resources:
    ItemsDynamoDbTable:
      Type: 'AWS::DynamoDB::Table'
      DeletionPolicy: Retain
      Properties:
        TableName: ${self:provider.environment.ROADMAP_ITEMS_TABLE}
        AttributeDefinitions:
          - AttributeName: roadmapItemId
            AttributeType: S
        KeySchema:
          - AttributeName: roadmapItemId
            KeyType: HASH
        ProvisionedThroughput:
          ReadCapacityUnits: 1
          WriteCapacityUnits: 1
```

### Hooking it up to our handlers

1. Update `CreateRoadmapsHandler` to use persistence:

```java
public class CreateRoadmapsHandler implements RequestHandler<RoadmapRequest, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(CreateRoadmapsHandler.class);
    private static final RoadmapItemsRepository itemsRepository = new RoadmapItemsRepository();

    @Override
    public ApiGatewayResponse handleRequest(RoadmapRequest request, Context context) {
        LOG.info("received Create Roadmap API request: " + request);

        Optional<RoadmapItem> newItem = request.getBodyAsRoadmapItem();
        if (newItem.isPresent()){
            RoadmapItem result = itemsRepository.createRoadmapItem(newItem.get());
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(result)
                    .build();
        } else {
            return ApiGatewayResponse.builder()
                    .setStatusCode(204)
                    .setObjectBody(new Response("Error creating RoadmapItem","Could probably not parse the JSON"))
                    .build();
        }
    }
    }
```

2. Update `GetRoadmapsHandler` to use persistence:

```java
//in GetRoadmapsHandler.java
    private ApiGatewayResponse handleSingleItemRequest(RoadmapRequest request) {
        String uuidStr = request.getPathParameters().getRoadmapItemId();
        UUID roadmapItemId = null;
        try {
            roadmapItemId = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            LOG.error("Unvalid UUID provided for roadmapItemId");
            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody(new Response("Error retrieving RoadmapItem","Unvalid UUID provided for roadmapItemId: "+uuidStr))
                    .build();
        }
        RoadmapItem result = itemsRepository.getRoadmapItem(roadmapItemId);
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(result)
                .build();
    }

    private ApiGatewayResponse handleItemsRequest(RoadmapRequest request){
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(itemsRepository.getRoadmapItems())
                .build();
    }
```

*Oops!* _Now our tests are failing!_
Answer: mock a bit! (or get a local DynamoDB emulator going..)
_For now we just bypass the test, and cover mocking in a next session._

## 5. Securing Credentials

Managing secure credentials (e.g. credentials to integrate other services, etc) can be painful. The *AWS SSM*'s _Paramater Store_ is an easy to use service for storing credentials needed by your serverless code.

See code in `aws-java-roadmaps-api-5` folder.

1. Add super secret key `/development/mysecrets/apikey` to SSM:

_This can be done via *aws-cli*, but for today we do it via the web console._

2. Add *AWS SSM SDK* lib to `build.gradle` dependencies:

```gradle
dependencies {
    compile (
            ..
            "com.amazonaws:aws-java-sdk-ssm:1.11.602",
            ..
    }
}
```

3. Add `SecureParameterService` class to retrieve it:

```java
package com.serverless.services;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import org.apache.log4j.Logger;

/**
 * Service class to give access to parameters stored on AWS Parameter Store
 */
public class SecureParameterService {
    private static final Logger LOG = Logger.getLogger(SecureParameterService.class);

    private static String superSecretApiKey;

    private SecureParameterService(){}

    public static String getParameterValue(String name, boolean withDecryption){
        final AWSSimpleSystemsManagement client = AWSSimpleSystemsManagementClientBuilder.defaultClient();

        GetParameterRequest request = new GetParameterRequest();
        request.withName(name).setWithDecryption(withDecryption);

        GetParameterResult result = client.getParameter(request);

        LOG.debug("SSM result for param "+name+": "+result);

        if (result.getParameter() != null){
            return result.getParameter().getValue();
        } else {
            return null;
        }
    }

    public static String getStageName(){
        String stage = System.getenv("STAGE");
        if (stage == null) stage = "development";

        return stage;
    }

    public static String getSuperSecretApiKey() {
        if (superSecretApiKey != null) return superSecretApiKey;

        String paramName = "/"+getStageName()+"/mysecrets/apikeyt";
        superSecretApiKey = getParameterValue(paramName, true);

        if (superSecretApiKey == null) throw new RuntimeException("Super secret is NULL!");

        return superSecretApiKey;
    }
}
```

4. Add `STAGE` env variable in `serverless.yml`:

```yaml
provider:
  ..
  environment:
    ROADMAP_ITEMS_TABLE: ${self:service}-${opt:stage, self:provider.stage}-roadmapitems
    STAGE: ${opt:stage, self:provider.stage}
```

5. Update `GetRoadmapsHandler` handler to use it:

```java
    public ApiGatewayResponse handleRequest(RoadmapRequest request, Context context) {
        LOG.info("received GET Roadmap API request: " + request);

        LOG.info("*** My super secret key: "+ SecureParameterService.getSuperSecretApiKey());

        if (request.getResource() != null && request.getResource().equals("/roadmapitems/{roadmapItemId}")) {
            return handleSingleItemRequest(request);
        }

        return handleItemsRequest(request);
    }
```

Test and .... error? What happened?!

6. Update IAM permissions in `serverless.yaml`:

```yaml
  ..
  iamRoleStatements:
    ..
    - Effect: Allow
      Action:
        - ssm:DescribeParameters
      Resource: "*"
    - Effect: Allow
      Action:
        - ssm:GetParameter
        - ssm:GetParameters
      Resource: "arn:aws:ssm:${opt:region, self:provider.region}:*:parameter/${opt:stage, self:provider.stage}/mysecrets/*"
```

## 6. Securing the API

These days we have to keep our doors locked, and the same goes for APIs. 
With *Serverless Framework* we can define the authorisation to be used on the API endpoints.

See code in `aws-java-roadmaps-api-6` folder.

There are ways to specify to use Cognito authorisation, or even a custom Lambda as authorizer, but for our example we want to make use of API Keys.

1. Define the API keys and usage plans in `serverless.yml`:

```yaml
provider:
  ..
  apiKeys:
    - ${self:service}-${opt:stage, self:provider.stage}-roadmaps-apikey
  usagePlan:
    quota:
      limit: 5000
      offset: 2
      period: MONTH
    throttle:
      burstLimit: 200
      rateLimit: 100
```

2. Also set the API endpoints to `private: true` which need a key:
   
```yaml
functions:
  create-handler:
    handler: com.serverless.handlers.CreateRoadmapsHandler
    events:
      - http:
          path: roadmapitems
          method: post
          cors: true
          private: true
```

## 7. Monitoring

TODO - enable tracing, Xray, Cloudwatch, etc (serverless params for tracing)
[optional] Datadog, others?

## 8. [Optional] Mocking AWS Services

TODO

## 8. [Optional] Using Swagger to define API

TODO

## 9. SQS and SNS

TODO

## 10. Lambda Layers

TODO

## 11. SES hookup

TODO: example to use SES to send emails when this tut is updated