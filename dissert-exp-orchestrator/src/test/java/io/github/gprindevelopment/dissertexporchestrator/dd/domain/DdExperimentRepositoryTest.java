package io.github.gprindevelopment.dissertexporchestrator.dd.domain;

import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentErrorEntity;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentRepository;
import io.github.gprindevelopment.dissertexporchestrator.dd.data.DdExperimentResultEntity;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DdExperimentRepositoryTest {
    @Autowired
    private DdExperimentRepository repository;
    private final EasyRandom generator = new EasyRandom(
            new EasyRandomParameters()
                    .excludeField((f) -> f.getName().equals("id"))
                    .excludeField((f) -> f.getName().equals("error"))
                    .excludeField((f) -> f.getName().equals("result"))
                    .excludeField((f) -> f.getName().equals("experimentId"))
                    .excludeField((f) -> f.getName().equals("experiment"))
    );

    @Test
    public void Should_persist_experiment_with_error_successfully() {
        DdExperimentEntity experiment = generator.nextObject(DdExperimentEntity.class);
        DdExperimentErrorEntity error = generator.nextObject(DdExperimentErrorEntity.class);
        error.setExperiment(experiment);
        experiment.setError(error);

        DdExperimentEntity saved = repository.save(experiment);
        assertNotNull(saved);
        assertNotNull(saved.getError());
        assertEquals(saved.getId(), saved.getError().getExperimentId());
    }

    @Test
    public void Should_persist_experiment_with_results_successfully() {
        DdExperimentEntity experiment = generator.nextObject(DdExperimentEntity.class);
        DdExperimentResultEntity result = generator.nextObject(DdExperimentResultEntity.class);
        result.setExperiment(experiment);
        experiment.setResult(result);

        DdExperimentEntity saved = repository.save(experiment);
        assertNotNull(saved);
        assertNotNull(saved.getResult());
        assertEquals(saved.getId(), saved.getResult().getExperimentId());
    }

    @Test
    public void Should_read_experiment_entity_successfully() {
        DdExperimentEntity experiment = generator.nextObject(DdExperimentEntity.class);
        DdExperimentResultEntity result = generator.nextObject(DdExperimentResultEntity.class);
        DdExperimentErrorEntity error = generator.nextObject(DdExperimentErrorEntity.class);

        error.setExperiment(experiment);
        result.setExperiment(experiment);
        experiment.setError(error);
        experiment.setResult(result);
        repository.save(experiment);

        assertTrue(repository.findAll().stream().findFirst().isPresent());
    }

}