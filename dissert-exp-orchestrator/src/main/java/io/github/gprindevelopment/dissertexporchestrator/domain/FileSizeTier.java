package io.github.gprindevelopment.dissertexporchestrator.domain;

import lombok.Getter;

@Getter
public enum FileSizeTier {
    TIER_1(10_000L),
    TIER_2(100_000L),
    TIER_3(1_000_000L),
    TIER_4(64_000_000L),
    TIER_5(128_000_000L),
    TIER_6(256_000_000L),
    TIER_7(512_000_000L),
    TIER_8(1_024_000_000L);

    FileSizeTier(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    private final long fileSizeBytes;
}
