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
