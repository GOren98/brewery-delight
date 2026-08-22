package dev.goren98.brewerydelight.aroma;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 6-2 foundation registry. The full data-driven aroma tree is populated in later 6-2/6-3 work;
 * this class establishes the lookup contract without changing the 6-1 CROP_AROMA transport.
 */
public final class AromaDefinitions {
    private static final Map<String, AromaDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        // Minimal representatives used to validate all four categories and progression ranks.
        register(new AromaDefinition("apple", AromaType.INGREDIENT, AromaRank.NORMAL));
        register(new AromaDefinition("kiwi", AromaType.INGREDIENT, AromaRank.NORMAL));
        register(new AromaDefinition("wild_yeast", AromaType.FERMENTATION, AromaRank.NORMAL));
        register(new AromaDefinition("fresh", AromaType.SENSATION, AromaRank.NORMAL));
        register(new AromaDefinition("oak", AromaType.AGING, AromaRank.UNIQUE));
    }

    public static void register(AromaDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
    }

    public static Optional<AromaDefinition> find(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        AromaDefinition known = DEFINITIONS.get(id);
        if (known != null) return Optional.of(known);
        // Every 6-1 crop's default aroma is a valid NORMAL ingredient aroma. This fallback keeps
        // all existing crops working until the complete aroma data set replaces it.
        return Optional.of(new AromaDefinition(id, AromaType.INGREDIENT, AromaRank.NORMAL));
    }

    private AromaDefinitions() {}
}
