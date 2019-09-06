package com.serverless.persistence.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.serverless.domain.RoadmapItem;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.UUID;

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

    public static RoadmapItemDTO fromItem(RoadmapItem item){
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
}
