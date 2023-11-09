package io.github.gprindevelopment.dissertexporchestrator.dd.domain;

import io.github.gprindevelopment.dissertexporchestrator.domain.DayOfWeek;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Builder
public class DdExpRecordEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String rawResponse;

    private Timestamp collectedAt;

    @Enumerated(EnumType.STRING)
    private SystemName systemName;

    private String command;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    private Long ioSizeBytes;

    private Long fileSizeBytes;

    private String rawThroughput;

    private String rawLatency;

    private Double latencySeconds;

    private Double throughputKbPerSecond;

    @Enumerated(EnumType.STRING)
    private TimeOfDay timeOfDay;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
}
