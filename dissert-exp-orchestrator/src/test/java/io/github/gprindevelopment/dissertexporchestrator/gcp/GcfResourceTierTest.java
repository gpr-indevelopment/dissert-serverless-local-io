package io.github.gprindevelopment.dissertexporchestrator.gcp;

import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GcfResourceTierTest {

    @ParameterizedTest
    @MethodSource("provideResourceTiers")
    public void Should_map_all_resource_tiers_successfully(ResourceTier resourceTier, GcfResourceTier expectedGcfResourceTier) {
        assertEquals(expectedGcfResourceTier, GcfResourceTier.from(resourceTier));
    }

    private static Stream<Arguments> provideResourceTiers() {
        return Stream.of(
                Arguments.of(ResourceTier.TIER_1, GcfResourceTier.TIER_1),
                Arguments.of(ResourceTier.TIER_2, GcfResourceTier.TIER_2),
                Arguments.of(ResourceTier.TIER_3, GcfResourceTier.TIER_3),
                Arguments.of(ResourceTier.TIER_4, GcfResourceTier.TIER_4),
                Arguments.of(ResourceTier.TIER_5, GcfResourceTier.TIER_5)
        );
    }
}