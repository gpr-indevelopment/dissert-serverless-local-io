package io.github.gprindevelopment.dissertexporchestrator.dd.data;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

class DdExperimentEntityTest {
    private final EasyRandom generator = new EasyRandom();

    @Test
    public void Should_avoid_circular_reference_on_toString() {
        System.out.println(generator.nextObject(DdExperimentEntity.class).toString());
    }

}