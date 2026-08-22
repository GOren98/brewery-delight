package dev.goren98.brewerydelight.brewing;

import dev.goren98.brewerydelight.alcohol.CoreAlcoholInput;
import dev.goren98.brewerydelight.alcohol.CoreAlcoholRecipe;
import dev.goren98.brewerydelight.registry.ModBlockEntities;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class BrewingStationBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SIZE = 2;
    public static final int DEFAULT_PROCESS_TIME = 200;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private int processTime = DEFAULT_PROCESS_TIME;
    private final ContainerData data = new SimpleContainerData(2) {
        @Override public int get(int index) { return index == 0 ? progress : processTime; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; else processTime = Math.max(1, value); }
    };

    public BrewingStationBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BREWING_STATION.get(), pos, state); }
    @Override protected Component getDefaultName() { return Component.translatable("container.brewerydelight.brewing_station"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new BrewingStationMenu(id, inventory, this, data); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return SIZE; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT && stack.is(ModItems.BASE_BOTTLE.get());
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.putInt("ProcessTime", processTime);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        processTime = tag.contains("ProcessTime") ? Math.max(1, tag.getInt("ProcessTime")) : DEFAULT_PROCESS_TIME;
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrewingStationBlockEntity station) {
        Optional<RecipeHolder<CoreAlcoholRecipe>> match = station.findRecipe(level);
        if (match.isEmpty()) {
            if (station.progress != 0) {
                station.progress = 0;
                station.processTime = DEFAULT_PROCESS_TIME;
                setChanged(level, pos, state);
            }
            return;
        }

        CoreAlcoholRecipe recipe = match.get().value();
        ItemStack result = recipe.assemble(new CoreAlcoholInput(station.items.get(SLOT_INPUT), "brew"), level.registryAccess());
        ItemStack output = station.items.get(SLOT_OUTPUT);
        if (!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, result) || output.getCount() >= output.getMaxStackSize())) return;

        station.processTime = recipe.processingTime();
        station.progress++;
        if (station.progress < station.processTime) return;

        station.items.get(SLOT_INPUT).shrink(1);
        if (output.isEmpty()) station.items.set(SLOT_OUTPUT, result);
        else output.grow(1);
        station.progress = 0;
        setChanged(level, pos, state);
    }

    private Optional<RecipeHolder<CoreAlcoholRecipe>> findRecipe(Level level) {
        ItemStack input = items.get(SLOT_INPUT);
        if (input.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.CORE_ALCOHOL_TYPE.get(), new CoreAlcoholInput(input, "brew"), level);
    }
}
