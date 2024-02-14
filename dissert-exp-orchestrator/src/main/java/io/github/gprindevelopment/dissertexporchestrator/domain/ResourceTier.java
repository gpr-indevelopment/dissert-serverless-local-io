package io.github.gprindevelopment.dissertexporchestrator.domain;

import lombok.Getter;

@Getter
public enum ResourceTier {
    TIER_1(128),
    TIER_2(256),
    TIER_3(512),
    TIER_4(1024),
    TIER_5(2048);

    ResourceTier(int memoryMbs) {
        this.memoryMbs = memoryMbs;
    }

    private final int memoryMbs;

    public boolean isCompatibleWith(FileSizeTier fileSizeTier) {
        return TierCompatibilityUtils.isCompatible(this, fileSizeTier);
    }
}
