package com.kafka.models;

public interface OffsetsMBean {

    String partitionsLagBetween(long low,
                                long high);

    long laggingPartitionCount(long low,
                               long high);

    String averageLag();

    long lastUpdatedTime();

    long errorsRecorded();
}
