package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

import java.util.Optional;

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
