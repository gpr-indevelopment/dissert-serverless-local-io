package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DdExperimentErrorEntity {

    @Id
    @Column(name = "EXPERIMENT_ID")
    private Long experimentId;
    @Lob
    private String rawError;
    @ToString.Exclude
    @OneToOne
    @MapsId
    @JoinColumn(name = "EXPERIMENT_ID")
    private DdExperimentEntity experiment;

}
