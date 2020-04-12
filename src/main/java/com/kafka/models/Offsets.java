package com.kafka.models;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.kafka.messages.CustomMessages;

public class Offsets implements OffsetsMBean {

    private List<OffsetRecord> offsetRecords = new ArrayList<>();

    private long lastUpdatedTime;
    private AtomicLong errorsRecorded = new AtomicLong();

    public Offsets() {

    }

    public Offsets(List<OffsetRecord> offsetRecords) {
        this.offsetRecords = offsetRecords;
    }

    public synchronized void setLastUpdatedTime() {
        this.lastUpdatedTime = Instant.now().getEpochSecond();
    }

    public void setOffsetRecords(final List<OffsetRecord> offsetRecords) {
        this.offsetRecords = offsetRecords;
        setLastUpdatedTime();
    }

    public void recordError() {
        errorsRecorded.incrementAndGet();
        setLastUpdatedTime();
    }

    @Override
    public synchronized String partitionsLagBetween(long low,
                                                    long high) {
        List<String> topicPartition = new ArrayList<>();
        offsetRecords.parallelStream().forEach(record -> {
            if (record.getLag() >= low
                        && (record.getLag() < high || high == -1)) {
                topicPartition.add(record.getTopic().concat("_").concat(
                            record.getPartition().concat("_").concat(
                                        String.valueOf(record.getLag()))));
            }
        });
        return topicPartition.stream().collect(Collectors.joining("|"));
    }

    @Override
    public synchronized long laggingPartitionCount(long low,
                                                   long high) {

        return offsetRecords.parallelStream().filter(record -> {
            if (record.getLag() >= low
                        && (record.getLag() < high || high == -1)) {
                return true;
            }
            return false;
        }).count();

    }

    @Override
    public synchronized String averageLag() {
        DecimalFormat df = new DecimalFormat("0.00");
        Optional<Long> totalLag = offsetRecords.stream().filter(
                    record -> record.getLag() > 0).map(
                                record -> record.getLag()).reduce(
                                            (a,
                                             b) -> a + b);
        long count = offsetRecords.parallelStream().filter(
                    record -> record.getLag() > 0).count();
        if (totalLag.isPresent()) {
            return df.format((totalLag.get() * 1.0) / count);
        }
        return CustomMessages.LAG_NOT_CALCULATED_SO_FAR;
    }

    @Override
    public long lastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    @Override
    public long errorsRecorded() {
        return errorsRecorded.get();
    }

}
