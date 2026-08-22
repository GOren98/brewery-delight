package dev.goren98.brewerydelight.barrel;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BarrelInventory implements Container {
    public static final int SEASONING_REQUIRED = 5;

    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final BarrelSavedData owner;

    private String woodId = "";
    private String aroma = "";
    private String lockedProduct = "";
    private String seasoningTarget = "";
    private int seasoningCount = 0;

    BarrelInventory(BarrelSavedData owner) { this.owner = owner; }
    void setItemSilently(int slot, ItemStack stack) { if (slot >= 0 && slot < 9) items.set(slot, stack); }

    public void configureWood(ResourceLocation id) {
        String nextWood = id.toString();
        if (woodId.equals(nextWood)) return;
        woodId = nextWood; aroma = id.getPath(); seasoningTarget = ""; seasoningCount = 0; owner.setDirty();
    }

    public String getWoodId() { return woodId; }
    public String getAroma() { return aroma; }
    public String getLockedProduct() { return lockedProduct; }
    public String getSeasoningTarget() { return seasoningTarget; }
    public int getSeasoningCount() { return seasoningCount; }

    public boolean isSeasoned() {
        if (woodId.isEmpty() || aroma.isEmpty()) return false;
        ResourceLocation id = ResourceLocation.tryParse(woodId);
        return id != null && !aroma.equals(id.getPath());
    }

    void loadMeta(String woodId, String aroma, String lockedProduct, String seasoningTarget, int seasoningCount) {
        this.woodId = woodId;
        this.aroma = aroma;
        this.lockedProduct = lockedProduct;
        // Previous saves used "product|aroma". Seasoning is now aroma-scoped, so migrate
        // the persisted target without losing the accumulated count.
        this.seasoningTarget = normalizeSeasoningTarget(seasoningTarget);
        this.seasoningCount = seasoningCount;
    }

    public void recordFullyAged(ItemStack stack) {
        if (isSeasoned()) return;

        // Only BREW(stage 1) and SPIRIT(stage 2) may season a barrel. Liqueurs can still
        // be aged, but their multi-aroma structure must never participate in seasoning.
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), -1);
        if (stage != 1 && stage != 2) return;

        // The seasoning source is the single primary aroma the alcohol had when aging
        // started. Aging aromas added by the barrel itself are deliberately ignored.
        int primaryLevel = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        if (primaryLevel < 5) return;
        String primary = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        if (primary.isEmpty()) return;

        if (!primary.equals(seasoningTarget)) {
            seasoningTarget = primary;
            seasoningCount = 1;
        } else if (seasoningCount < SEASONING_REQUIRED) {
            seasoningCount++;
        }

        if (seasoningCount >= SEASONING_REQUIRED) {
            aroma = primary;
            seasoningTarget = "";
            seasoningCount = 0;
        }
        owner.setDirty();
    }

    private static String normalizeSeasoningTarget(String target) {
        if (target == null || target.isEmpty()) return "";
        int split = target.indexOf('|');
        return split >= 0 && split + 1 < target.length() ? target.substring(split + 1) : target;
    }

    @Override public int getContainerSize() { return 9; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack out = items.get(slot).split(amount);
        if (!out.isEmpty()) { out.remove(ModComponents.STARTED_AT.get()); refreshLockIfEmpty(); setChanged(); }
        return out;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack out = items.get(slot); items.set(slot, ItemStack.EMPTY);
        if (!out.isEmpty()) out.remove(ModComponents.STARTED_AT.get());
        refreshLockIfEmpty(); owner.setDirty(); return out;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !canPlaceItem(slot, stack)) return;
        items.set(slot, stack); if (stack.getCount() > 1) stack.setCount(1);
        if (!stack.isEmpty()) {
            String product = productOf(stack); if (lockedProduct.isEmpty()) lockedProduct = product;
            if (stack.getOrDefault(ModComponents.STARTED_AT.get(), 0L) == 0L) stack.set(ModComponents.STARTED_AT.get(), System.currentTimeMillis());
        }
        refreshLockIfEmpty(); setChanged();
    }
    private void refreshLockIfEmpty() { if (isEmpty()) lockedProduct = ""; }
    @Override public void setChanged() { owner.setDirty(); }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { items.clear(); lockedProduct = ""; setChanged(); }
    @Override public int getMaxStackSize() { return 1; }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (!isSupportedBottle(stack)) return false;
        String product = productOf(stack);
        return !product.isEmpty() && (lockedProduct.isEmpty() || lockedProduct.equals(product));
    }

    public static boolean isSupportedBottle(ItemStack stack) {
        if (stack.is(ModItems.TEST_SPIRIT.get()) || stack.is(ModItems.TEST_LIQUEUR.get())) return true;
        if (stack.is(ModItems.TEST_BREW.get())) return stack.getOrDefault(ModComponents.STAGE.get(), 0) >= 1;
        if (stack.is(ModItems.SPIRIT_BOTTLE.get()) || stack.is(ModItems.LIQUEUR_BOTTLE.get())) return true;
        // Brewing Pot Base bottles and stage-0 legacy Brew bottles are deliberately rejected.
        return stack.is(ModItems.BREW_BOTTLE.get()) && stack.getOrDefault(ModComponents.STAGE.get(), 0) >= 1;
    }

    public static String productOf(ItemStack stack) {
        String existing = stack.getOrDefault(ModComponents.PRODUCT_ID.get(), "");
        if (!existing.isEmpty()) return existing;
        if (stack.is(ModItems.TEST_SPIRIT.get())) return "test_spirit";
        if (stack.is(ModItems.TEST_BREW.get())) return "test_brew";
        if (stack.is(ModItems.TEST_LIQUEUR.get())) return "test_liqueur";
        return "";
    }
}
