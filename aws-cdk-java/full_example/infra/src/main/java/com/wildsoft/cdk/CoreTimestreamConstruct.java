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
     * Multi-measure table to store different events from tracto app.
     * measure name == event type
     * dimensions: childId, userId,
     * measures: value, timestamp, ???
     */
    private final CfnTable eventsTable;

    /**
     * multi-measure table
     * measure name == assessment type
     * dimensions: childId, userId,
     * measures: timestamp, question, value, tags, ???
     */
    private final CfnTable assessmentsTable;

    /**
     * single measure table
     * dimensions: timestamp, userId,
     * measure name: type of config item
     * value: config value
     */
    private final CfnTable configurationEntryTable;

    /**
     * multi-measure table
     * dimensions: childId, userId, timestamp
     * measure name: ??
     * measures: height, weight, age?, conditions?
     */
    private final CfnTable childProfileSnapshotsTable;

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

        this.assessmentsTable = new CfnTable(this, "CoreTimestreamTableAssessments",
                CfnTableProps.builder()
                        .tableName("core-ts-assessments")
                        .databaseName(dbName)
                        //.retentionProperties(???)
                        .build());
        this.assessmentsTable.getNode().addDependency(this.timeStreamDB);

        this.configurationEntryTable = new CfnTable(this, "CoreTimestreamTableConfig",
                CfnTableProps.builder()
                        .tableName("core-ts-config")
                        .databaseName(dbName)
                        //.retentionProperties(???)
                        .build());
        this.configurationEntryTable.getNode().addDependency(this.timeStreamDB);

        this.childProfileSnapshotsTable = new CfnTable(this, "CoreTimestreamTableProfiles",
                CfnTableProps.builder()
                        .tableName("core-ts-profilesnapshots")
                        .databaseName(dbName)
                        //.retentionProperties(???)
                        .build());
        this.childProfileSnapshotsTable.getNode().addDependency(this.timeStreamDB);
    }
}
