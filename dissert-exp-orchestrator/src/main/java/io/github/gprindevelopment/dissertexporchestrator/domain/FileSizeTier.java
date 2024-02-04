package io.github.gprindevelopment.dissertexporchestrator.domain;

import lombok.Getter;

@Getter
public enum FileSizeTier {
    TIER_1(128_000_000L),
    TIER_2(256_000_000L),
    TIER_3(512_000_000L),
    TIER_4(1_024_000_000L),
    TIER_5(2_048_000_000L),
    TIER_6(4_096_000_000L),
    TIER_7(8_192_000_000L);

    FileSizeTier(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    private final long fileSizeBytes;
}
