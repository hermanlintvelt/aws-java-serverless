package com.wildsoft.cdk;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.timestream.CfnDatabase;
import software.amazon.awscdk.services.timestream.CfnDatabaseProps;
import software.amazon.awscdk.services.timestream.CfnTable;
import software.amazon.awscdk.services.timestream.CfnTableProps;
import software.constructs.Construct;

public class CoreTimestreamConstruct extends Construct {
    private final CfnDatabase timeStreamDB;

    //TODO: see timesteam best practices: https://docs.aws.amazon.com/timestream/latest/developerguide/data-modeling.html
    /**
     * Multi-measure table to store different events from the app.
     * measure name == event type
     * dimensions: userId,
     * measures: value, timestamp, ???
     */
    private final CfnTable eventsTable;

    public CoreTimestreamConstruct(@NotNull Construct scope, @NotNull String id) {
        super(scope, id);
        final String dbName = "core-input-timestream";
        this.timeStreamDB = new CfnDatabase(this, "CoreTimestreamDatabase",
                CfnDatabaseProps.builder()
                .databaseName(dbName).build()
        );

        //TODO: figure out retentionProperties
        this.eventsTable = new CfnTable(this, "CoreTimestreamTableEvents",
                CfnTableProps.builder()
                        .tableName("core-ts-events")
                        .databaseName(dbName)
                        //.retentionProperties(???)
                        .build());
        this.eventsTable.getNode().addDependency(this.timeStreamDB);

    }
}
