package io.github.gprindevelopment.dissertexporchestrator.aws;

import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaResourceTierTest {

    @ParameterizedTest
    @MethodSource("provideResourceTiers")
    public void Should_map_all_resource_tiers_successfully(ResourceTier resourceTier, LambdaResourceTier expectedLambdaResourceTier) {
        assertEquals(expectedLambdaResourceTier, LambdaResourceTier.from(resourceTier));
    }

    private static Stream<Arguments> provideResourceTiers() {
        return Stream.of(
                Arguments.of(ResourceTier.TIER_1, LambdaResourceTier.TIER_1),
                Arguments.of(ResourceTier.TIER_2, LambdaResourceTier.TIER_2),
                Arguments.of(ResourceTier.TIER_3, LambdaResourceTier.TIER_3),
                Arguments.of(ResourceTier.TIER_4, LambdaResourceTier.TIER_4),
                Arguments.of(ResourceTier.TIER_5, LambdaResourceTier.TIER_5)
        );
    }

}