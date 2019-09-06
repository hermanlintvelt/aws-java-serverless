package com.serverless.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class RoadmapRequest {
    private static final Logger LOG = Logger.getLogger(RoadmapRequest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    public Optional<RoadmapItem> getBodyAsRoadmapItem(){
        try {
            RoadmapItem item = objectMapper.readValue(this.body, RoadmapItem.class);
            return Optional.of(item);
        } catch (IOException e) {
            LOG.error("Error parsing body of request as RoadmapItem object: "+e.getMessage());
            return Optional.empty();
        }
    }

    public RoadmapPathParameters getPathParameters() {
        if (pathParameters == null) return  new RoadmapPathParameters();
        return pathParameters;
    }

    public void setPathParameters(RoadmapPathParameters pathParameters) {
        this.pathParameters = pathParameters;
    }

    @Override
    public String toString() {
        return "RoadmapRequest{" +
                "resource='" + resource + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", body='" + body + '\'' +
                ", pathParameters=" + pathParameters +
                '}';
    }

    public static class RoadmapPathParameters {
        private String roadmapItemId;

        public RoadmapPathParameters(){}

        public RoadmapPathParameters(String roadmapId) {
            this.roadmapItemId = roadmapId;
        }

        public String getRoadmapItemId() {
            return roadmapItemId;
        }

        public void setRoadmapItemId(String roadmapItemId) {
            this.roadmapItemId = roadmapItemId;
        }

        @Override
        public String toString() {
            return "RoadmapPathParameters{" +
                    "roadmapItemId='" + roadmapItemId + '\'' +
                    '}';
        }
    }
}
