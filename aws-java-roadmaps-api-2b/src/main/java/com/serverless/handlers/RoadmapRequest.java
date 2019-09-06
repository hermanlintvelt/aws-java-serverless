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
