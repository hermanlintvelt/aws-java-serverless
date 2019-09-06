package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.domain.RoadmapItem;
import com.serverless.persistence.dynamodb.RoadmapItemsRepository;
import org.apache.log4j.Logger;

import java.util.Optional;

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
