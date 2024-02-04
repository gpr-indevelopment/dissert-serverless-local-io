package io.github.gprindevelopment.dissertexporchestrator.gcp;


import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public enum GcfResourceTier {
    TIER_1("128Mi", "0.08"),
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
        return resourceTierMap.get(resourceTier);
    }

    private final String memory;

    private final String cpu;

    private static final Map<ResourceTier, GcfResourceTier> resourceTierMap = Map.of(
            ResourceTier.TIER_1, GcfResourceTier.TIER_1,
            ResourceTier.TIER_2, GcfResourceTier.TIER_2,
            ResourceTier.TIER_3, GcfResourceTier.TIER_3,
            ResourceTier.TIER_4, GcfResourceTier.TIER_4,
            ResourceTier.TIER_5, GcfResourceTier.TIER_5,
            ResourceTier.TIER_6, GcfResourceTier.TIER_6,
            ResourceTier.TIER_7, GcfResourceTier.TIER_7
    );
}
