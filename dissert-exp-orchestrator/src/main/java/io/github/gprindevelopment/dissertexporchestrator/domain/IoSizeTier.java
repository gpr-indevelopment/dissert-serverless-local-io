package io.github.gprindevelopment.dissertexporchestrator.domain;

import lombok.Getter;

@Getter
public enum IoSizeTier {
    TIER_1(500L),
    TIER_2(1_000L),
    TIER_3(2_000L),
    TIER_4(4_000L),
    TIER_5(8_000L),
    TIER_6(16_000L),
    TIER_7(32_000L),
    TIER_8(64_000L),
    TIER_9(128_000L);

    IoSizeTier(long ioSizeBytes) {
        this.ioSizeBytes = ioSizeBytes;
    }
    private final long ioSizeBytes;

    public boolean isCompatibleWith(FileSizeTier fileSizeTier) {
        return TierCompatibilityUtils.isCompatible(fileSizeTier, this);
    }
}
