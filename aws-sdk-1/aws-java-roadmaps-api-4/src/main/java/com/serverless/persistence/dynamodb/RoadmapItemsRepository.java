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
