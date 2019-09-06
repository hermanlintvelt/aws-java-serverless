package com.serverless.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.serverless.json.LocalDateDeserializer;
import com.serverless.json.LocalDateSerializer;

import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadmapItem {
    public enum PriorityType {
        NOW, NEXT, LATER
    }

    private UUID roadmapItemId;
    private String name;
    private String description;
    private PriorityType priorityType;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate milestoneDate;

    public RoadmapItem(){}

    public RoadmapItem(UUID roadmapItemId, String name, String description, PriorityType priorityType, LocalDate milestoneDate) {
        this.roadmapItemId = roadmapItemId;
        this.name = name;
        this.description = description;
        this.priorityType = priorityType;
        this.milestoneDate = milestoneDate;
    }

    public UUID getRoadmapItemId() {
        return roadmapItemId;
    }

    public void setRoadmapItemId(UUID roadmapItemId) {
        this.roadmapItemId = roadmapItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }

    public LocalDate getMilestoneDate() {
        return milestoneDate;
    }

    public void setMilestoneDate(LocalDate milestoneDate) {
        this.milestoneDate = milestoneDate;
    }

    @Override
    public String toString() {
        return "RoadmapItem{" +
                "roadmapItemId=" + roadmapItemId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priorityType=" + priorityType +
                ", milestoneDate=" + milestoneDate +
                '}';
    }
}
