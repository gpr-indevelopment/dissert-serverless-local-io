package io.github.gprindevelopment.dissertexporchestrator.common;

import io.github.gprindevelopment.dissertexporchestrator.common.OperationType;
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

    private String systemName;

    private String command;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    private Long ioSizeBytes;

    private Long fileSizeBytes;

    private String rawThroughput;

    private String rawLatency;
}
