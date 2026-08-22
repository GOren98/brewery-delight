package dev.goren98.brewerydelight.aroma;

/** Metadata for an aroma ID. ItemStacks continue to store only the ID in CROP_AROMA. */
public record AromaDefinition(String id, AromaType type, AromaRank rank) {
    public AromaDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Aroma id must not be blank");
    }
}
