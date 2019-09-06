package com.serverless.persistence.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.apache.log4j.Logger;

public class DynamoDBConfig {
    private static final Logger LOG = Logger.getLogger(DynamoDBConfig.class);

    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
        builder.setTableNameResolver(new CustomNameResolver());
        return builder.build();
    }

    private static class CustomNameResolver implements DynamoDBMapperConfig.TableNameResolver {
        public String getTableName(Class<?> clazz, DynamoDBMapperConfig config) {
            if (clazz.equals(RoadmapItemDTO.class)){
                String tableName = System.getenv("ROADMAP_ITEMS_TABLE");
                LOG.info("RoadmapItems DBTable name configuration with name: "+tableName);
                return tableName;
            }
            else return "UnknownTable";
        }
    }
}