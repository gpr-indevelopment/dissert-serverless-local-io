package io.github.gprindevelopment.dissertexporchestrator.aws;

import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public enum LambdaResourceTier {
    TIER_1(128),
    TIER_2(256),
    TIER_3(512),
    TIER_4(1024),
    TIER_5(2048),
    TIER_6(4096),
    TIER_7(8192);

    LambdaResourceTier(int memory) {
        this.memory = memory;
    }

    public static LambdaResourceTier from(ResourceTier resourceTier) {
        return resourceTierMap.get(resourceTier);
    }

    private final int memory;

    private static final Map<ResourceTier, LambdaResourceTier> resourceTierMap = Map.of(
            ResourceTier.TIER_1, LambdaResourceTier.TIER_1,
            ResourceTier.TIER_2, LambdaResourceTier.TIER_2,
            ResourceTier.TIER_3, LambdaResourceTier.TIER_3,
            ResourceTier.TIER_4, LambdaResourceTier.TIER_4,
            ResourceTier.TIER_5, LambdaResourceTier.TIER_5,
            ResourceTier.TIER_6, LambdaResourceTier.TIER_6,
            ResourceTier.TIER_7, LambdaResourceTier.TIER_7
    );
}
