package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DdExperimentService {
    private final DdExperimentRepository repository;
    private final ExperimentFactory experimentFactory;

    public DdExperimentEntity recordSuccessfulExperiment(SuccessfulExperiment successfulExperiment) {
        DdExperimentEntity experiment = experimentFactory.buildExperiment(successfulExperiment);
        DdExperimentResultEntity result = DdExperimentResultEntity
                .builder()
                .rawThroughput(successfulExperiment.rawThroughput())
                .rawLatency(successfulExperiment.rawLatency())
                .rawResponse(successfulExperiment.rawResponse())
                .latencySeconds(extractLatency(successfulExperiment.rawLatency()))
                .throughputKbPerSecond(extractThroughputKbs(successfulExperiment.rawThroughput()))
                .experiment(experiment)
                .build();
        experiment.setResult(result);
        return repository.save(experiment);
    }

    public DdExperimentEntity recordFailedExperiment(FailedExperiment failedExperiment) {
        DdExperimentEntity experiment = experimentFactory.buildExperiment(failedExperiment);
        DdExperimentErrorEntity error = DdExperimentErrorEntity
                .builder()
                .experiment(experiment)
                .rawError(failedExperiment.rawError())
                .build();
        experiment.setError(error);
        return repository.save(experiment);
    }

    protected Double extractThroughputKbs(String rawThroughput) {
        String[] splitRawThroughput = rawThroughput.split("\s");
        Double value = Double.parseDouble(splitRawThroughput[0]);
        String unit = splitRawThroughput[1];
        double throughputBytes = value * resolveMultiplierFromUnit(unit);
        return throughputBytes / 1e3;
    }

    private Double resolveMultiplierFromUnit(String unit) {
        double multiplier;
        switch (unit) {
            case "GB/s" -> multiplier = 1e9;
            case "MB/s" -> multiplier = 1e6;
            case "kB/s" -> multiplier = 1e3;
            default -> {
                multiplier = 0;
                log.warn("Unknown throughput unit: {}. Multiplier is set to {}.", unit, multiplier);
            }
        }
        return multiplier;
    }

    private Double extractLatency(String rawLatency) {
        return Double.parseDouble(rawLatency.split("\s")[0]);
    }
}
