package io.github.gprindevelopment.dissertexporchestrator.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TierCompatibilityUtilsTest {

    @ParameterizedTest
    @MethodSource("provideAllTierCombinations")
    public void File_sizes_bigger_or_equal_to_resource_allocation_should_not_be_compatible(ResourceTier resourceTier, FileSizeTier fileSizeTier) {
        if (fileSizeTier.getFileSizeBytes() >= resourceTier.getMemoryMbs() * 1e6) {
            assertFalse(TierCompatibilityUtils.isCompatible(resourceTier, fileSizeTier));
        }
    }

    @ParameterizedTest
    @MethodSource("provideAllTierCombinations")
    public void File_sizes_smaller_than_resource_allocation_are_compatible(ResourceTier resourceTier, FileSizeTier fileSizeTier) {
        if (fileSizeTier.getFileSizeBytes() < resourceTier.getMemoryMbs() * 1e6) {
            assertTrue(TierCompatibilityUtils.isCompatible(resourceTier, fileSizeTier));
        }
    }

    private static List<Arguments> provideAllTierCombinations() {
        List<Arguments> arguments = new ArrayList<>();
        for (ResourceTier resourceTier : ResourceTier.values()) {
            for (FileSizeTier fileSizeTier : FileSizeTier.values()) {
                arguments.add(Arguments.of(resourceTier, fileSizeTier));
            }
        }
        return arguments;
    }
}