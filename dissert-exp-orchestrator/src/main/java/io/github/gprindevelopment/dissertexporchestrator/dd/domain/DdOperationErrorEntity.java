package io.github.gprindevelopment.dissertexporchestrator.dd.domain;

import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import io.github.gprindevelopment.dissertexporchestrator.domain.WeekPeriod;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.DayOfWeek;

@Entity
@Data
public class DdOperationErrorEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private SystemName systemName;

    private String command;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    private Long ioSizeBytes;

    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    private TimeOfDay timeOfDay;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    private WeekPeriod weekPeriod;

    @Enumerated(EnumType.STRING)
    private ResourceTier resourceTier;

    private Timestamp occurredAt;
}
