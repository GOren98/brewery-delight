package dev.goren98.brewerydelight.aroma;

public enum ItemAromaClass {
    BREEDABLE_BASE(true, true, true),
    FIXED_BASE(true, true, false),
    AROMA_ONLY(false, true, false),
    NORMAL_ITEM(false, false, false);

    private final boolean canMakeBase;
    private final boolean hasAroma;
    private final boolean canReceiveAroma;

    ItemAromaClass(boolean canMakeBase, boolean hasAroma, boolean canReceiveAroma) {
        this.canMakeBase = canMakeBase;
        this.hasAroma = hasAroma;
        this.canReceiveAroma = canReceiveAroma;
    }

    public boolean canMakeBase() { return canMakeBase; }
    public boolean hasAroma() { return hasAroma; }
    public boolean canReceiveAroma() { return canReceiveAroma; }
}
