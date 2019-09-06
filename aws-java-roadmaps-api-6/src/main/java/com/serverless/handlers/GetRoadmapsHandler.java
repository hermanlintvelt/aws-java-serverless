package com.serverless.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.domain.RoadmapItem;
import com.serverless.persistence.dynamodb.RoadmapItemsRepository;
import com.serverless.services.SecureParameterService;
import org.apache.log4j.Logger;

import java.util.UUID;

public class GetRoadmapsHandler implements RequestHandler<RoadmapRequest, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(GetRoadmapsHandler.class);
	private static final RoadmapItemsRepository itemsRepository = new RoadmapItemsRepository();

	@Override
	public ApiGatewayResponse handleRequest(RoadmapRequest request, Context context) {
		LOG.info("received GET Roadmap API request: " + request);

		LOG.info("*** My super secret key: "+ SecureParameterService.getSuperSecretApiKey());

		if (request.getResource() != null && request.getResource().equals("/roadmapitems/{roadmapItemId}")) {
			return handleSingleItemRequest(request);
		}

		return handleItemsRequest(request);
	}

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

}
