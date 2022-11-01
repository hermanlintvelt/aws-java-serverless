package com.wildsoft.core.lambda;

import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MyInvokableHandler implements RequestStreamHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public interface MyHandlerService {
        @LambdaFunction(functionName="my-unique-function-name")
        SomeResponse sendCustomRequest(MyCustomRequest request);
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)))) {

            MyCustomRequest request = OBJECT_MAPPER.readValue(reader, MyCustomRequest.class);
            logger.log("Some request received: " + request);

            //TODO do something with all this

            writer.write(OBJECT_MAPPER.writeValueAsString(new SomeResponse("Are you happy?", true)));
        } catch (JsonParseException | JsonMappingException e) {
            throw new RuntimeException("Oops, I do not understand the request!", e);
        }
    }

    public static class MyCustomRequest {
        private String type;
        private String filter;

        public MyCustomRequest() {
        }

        public MyCustomRequest(String type, String filter) {
            this.type = type;
            this.filter = filter;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        @Override
        public String toString() {
            return "MyCustomRequest{" +
                    "type='" + type + '\'' +
                    ", filter='" + filter + '\'' +
                    '}';
        }
    }

    public static class SomeResponse {
        private String message;
        private Boolean happy;

        public SomeResponse() {
        }

        public SomeResponse(String message, Boolean happy) {
            this.message = message;
            this.happy = happy;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getHappy() {
            return happy;
        }

        public void setHappy(Boolean happy) {
            this.happy = happy;
        }

        @Override
        public String toString() {
            return "SomeResponse{" +
                    "message='" + message + '\'' +
                    ", happy=" + happy +
                    '}';
        }
    }
}
