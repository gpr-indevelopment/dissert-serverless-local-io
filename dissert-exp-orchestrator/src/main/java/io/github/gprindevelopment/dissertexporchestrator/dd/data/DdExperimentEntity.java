package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExperimentName;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdOperationStatus;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.OperationType;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import io.github.gprindevelopment.dissertexporchestrator.domain.WeekPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.DayOfWeek;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DdExperimentEntity {
    @Id
    @GeneratedValue
    private Long id;
    private Timestamp occurredAt;
    @Enumerated(EnumType.STRING)
    private DdOperationStatus status;
    private String command;
    private Long ioSizeBytes;
    private Long fileSizeBytes;
    @Enumerated(EnumType.STRING)
    private OperationType operationType;
    @Enumerated(EnumType.STRING)
    private SystemName systemName;
    @Enumerated(EnumType.STRING)
    private DdExperimentName experimentName;
    @Enumerated(EnumType.STRING)
    private ResourceTier resourceTier;
    @Enumerated(EnumType.STRING)
    private TimeOfDay timeOfDay;
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    @Enumerated(EnumType.STRING)
    private WeekPeriod weekPeriod;
    @OneToOne(mappedBy = "experiment", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private DdExperimentErrorEntity error;
    @OneToOne(mappedBy = "experiment", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private DdExperimentResultEntity result;
}
