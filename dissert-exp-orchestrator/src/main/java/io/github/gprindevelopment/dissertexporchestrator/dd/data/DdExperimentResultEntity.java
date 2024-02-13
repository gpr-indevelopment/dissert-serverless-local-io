package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DdExperimentResultEntity {

    @Id
    @Column(name = "EXPERIMENT_ID")
    private Long experimentId;
    private String rawResponse;
    private String rawThroughput;
    private String rawLatency;
    private Double latencySeconds;
    private Double throughputKbPerSecond;
    @OneToOne
    @MapsId
    @JoinColumn(name = "EXPERIMENT_ID")
    private DdExperimentEntity experiment;
}
