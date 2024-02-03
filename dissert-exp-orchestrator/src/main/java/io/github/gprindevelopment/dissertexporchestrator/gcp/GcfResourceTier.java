package io.github.gprindevelopment.dissertexporchestrator.gcp;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum GcfResourceTier {
    TIER_1("128M", "0.083"),
    TIER_2("256M", "0.167"),
    TIER_3("512M", "0.333"),
    TIER_4("1G", "0.583"),
    TIER_5("2G", "1"),
    TIER_6("4G", "2"),
    TIER_7("8G", "2"),
    TIER_8("16G", "4"),
    TIER_9("32G", "8");

    GcfResourceTier(String memory, String cpu) {
        this.memory = memory;
        this.cpu = cpu;
    }

    private final String memory;

    private final String cpu;
}
