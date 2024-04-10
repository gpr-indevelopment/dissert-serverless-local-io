package io.github.gprindevelopment.dissertexporchestrator.domain;

import lombok.Getter;

@Getter
public enum IoSizeTier {
    TIER_1(512L, "512"),
    TIER_2(1_000L, "1k"),
    TIER_3(2_000L, "2k"),
    TIER_4(4_000L, "4k"),
    TIER_5(8_000L, "8k"),
    TIER_6(16_000L, "16k"),
    TIER_7(32_000L, "32k"),
    TIER_8(64_000L, "64k"),
    TIER_9(128_000L, "128k");

    IoSizeTier(long ioSizeBytes, String stringNotationBytes) {
        this.ioSizeBytes = ioSizeBytes;
        this.stringNotationBytes = stringNotationBytes;
    }
    private final long ioSizeBytes;

    private final String stringNotationBytes;

    public boolean isCompatibleWith(FileSizeTier fileSizeTier) {
        return TierCompatibilityUtils.isCompatible(fileSizeTier, this);
    }
}
