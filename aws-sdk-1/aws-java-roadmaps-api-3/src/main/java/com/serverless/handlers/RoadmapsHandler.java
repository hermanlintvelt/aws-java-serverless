package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
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
