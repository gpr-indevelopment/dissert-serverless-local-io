package io.github.gprindevelopment.dissertexporchestrator.gcp;


import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum GcfResourceTier {
    TIER_1("128M", "0.08"),
    TIER_2("256M", "0.145"),
    TIER_3("512M", "0.289"),
    TIER_4("1024M", "0.579"),
    TIER_5("2048M", "1"),
    TIER_6("4096M", "2"),
    TIER_7("8192M", "4");

    GcfResourceTier(String memory, String cpu) {
        this.memory = memory;
        this.cpu = cpu;
    }

    public static GcfResourceTier from(ResourceTier resourceTier) {
        return GcfResourceTier.TIER_4;
    }

    private final String memory;

    private final String cpu;
}
