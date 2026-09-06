package dev.goren98.brewerydelight.aroma;

/** Metadata loaded once from the bundled Aroma JSON. ItemStacks store only the stable Aroma ID. */
public record AromaDefinition(String id, String displayName, AromaType type, AromaRank rank,
                              String ingredientFamily, String line, String parent) {
    public AromaDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Aroma id must not be blank");
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        ingredientFamily = ingredientFamily == null ? "" : ingredientFamily;
        line = line == null ? "" : line;
        parent = parent == null ? "" : parent;
    }

    public AromaDefinition(String id, AromaType type, AromaRank rank) {
        this(id, id, type, rank, type == AromaType.INGREDIENT ? id : "", "", "");
    }
}
