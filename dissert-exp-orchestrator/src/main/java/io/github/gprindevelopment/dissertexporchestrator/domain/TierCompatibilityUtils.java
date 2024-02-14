package io.github.gprindevelopment.dissertexporchestrator.domain;

public final class TierCompatibilityUtils {
    public static boolean isCompatible(ResourceTier resourceTier, FileSizeTier fileSizeTier) {
        return fileSizeTier.getFileSizeBytes() < resourceTier.getMemoryMbs() * 1e6;
    }
}
