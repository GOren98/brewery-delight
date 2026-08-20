package dev.goren98.brewerydelight.cooking;

import dev.goren98.brewerydelight.registry.ModBlockEntities;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CookingPotBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_WATER = 0;
    public static final int SLOT_INGREDIENT_START = 1;
    public static final int SLOT_OUTPUT = 5;
    public static final int SIZE = 6;
    public static final int COOK_TIME = 100;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private final ContainerData data = new SimpleContainerData(2) {
        @Override public int get(int index) { return index == 0 ? progress : COOK_TIME; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; }
    };

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOKING_POT.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.brewerydelight.cooking_pot");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CookingPotMenu(containerId, inventory, this, data);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return SIZE; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        if (pot.matchesLegacyCider() && pot.canAcceptCiderOutput()) {
            pot.progress++;
            if (pot.progress >= COOK_TIME) {
                pot.craftLegacyCider();
                pot.progress = 0;
                setChanged(level, pos, state);
            }
        } else if (pot.progress != 0) {
            pot.progress = 0;
            setChanged(level, pos, state);
        }
    }

    private boolean matchesLegacyCider() {
        if (!items.get(SLOT_WATER).is(Items.WATER_BUCKET)) return false;
        for (int i = SLOT_INGREDIENT_START; i < SLOT_OUTPUT; i++) if (!items.get(i).is(Items.APPLE)) return false;
        return true;
    }

    private boolean canAcceptCiderOutput() {
        ItemStack output = items.get(SLOT_OUTPUT);
        return output.isEmpty() || (output.is(ModItems.BREW_BOTTLE.get()) && output.getCount() <= output.getMaxStackSize() - 4);
    }

    private void craftLegacyCider() {
        items.set(SLOT_WATER, new ItemStack(Items.BUCKET));
        for (int i = SLOT_INGREDIENT_START; i < SLOT_OUTPUT; i++) items.get(i).shrink(1);

        ItemStack result = new ItemStack(ModItems.BREW_BOTTLE.get(), 4);
        result.set(ModComponents.PRODUCT_ID.get(), "cider");
        result.set(ModComponents.DISPLAY_NAME.get(), "Cider");
        result.set(ModComponents.STAGE.get(), 0);
        result.set(ModComponents.PRIMARY_AROMA.get(), "apple");
        result.set(ModComponents.PRIMARY_LEVEL.get(), 4);
        result.set(ModComponents.FERMENTABLE.get(), true);
        result.set(ModComponents.COLOR.get(), 13933125);
        result.set(ModComponents.DISTILL_PRODUCT_ID.get(), "apple_brandy");
        result.set(ModComponents.DISTILL_DISPLAY_NAME.get(), "Apple Brandy");
        result.set(ModComponents.DISTILL_COLOR.get(), 12879668);

        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) items.set(SLOT_OUTPUT, result); else output.grow(4);
        setChanged();
    }
}
