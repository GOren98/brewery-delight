package dev.goren98.brewerydelight.barrel;

import java.util.Map;

/** Special wood profiles produced by seasoning a supported barrel with a matching primary Aroma. */
public final class BarrelSeasoningAromas {
    private static final Map<String, Map<String, String>> RECIPES = Map.of(
            "oak", Map.of(
                    "bold", "american_oak",
                    "elegant", "french_oak",
                    "structured", "hungarian_oak",
                    "mellow", "slavonian_oak",
                    "fragrant", "mizunara_oak"
            ),
            "birch", Map.of(
                    "fresh", "silver_birch",
                    "delicate", "paper_birch",
                    "rich", "yellow_birch",
                    "sweet", "sweet_birch"
            )
    );

    public static String resolve(String baseWoodAroma, String seasoningAroma) {
        return RECIPES.getOrDefault(baseWoodAroma, Map.of()).getOrDefault(seasoningAroma, seasoningAroma);
    }

    private BarrelSeasoningAromas() {}
}
