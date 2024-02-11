package io.github.gprindevelopment.dissertexporchestrator.dd.domain;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class DdOperationErrorRepositoryTest {

    @Autowired
    private DdOperationErrorRepository repository;
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_persist_dd_operation_error_successfully() {
        DdOperationErrorEntity entity = generator.nextObject(DdOperationErrorEntity.class);
        DdOperationErrorEntity saved = repository.save(entity);
        assertNotNull(saved);
    }

    @Test
    public void Should_read_dd_operation_error_from_database_successfully() {
        repository.save(generator.nextObject(DdOperationErrorEntity.class));
        assertTrue(repository.findAll().stream().findFirst().isPresent());
    }
}