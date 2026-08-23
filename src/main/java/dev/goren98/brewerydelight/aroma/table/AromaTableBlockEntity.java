package dev.goren98.brewerydelight.aroma.table;

import dev.goren98.brewerydelight.aroma.AromaItems;
import dev.goren98.brewerydelight.registry.ModBlockEntities;
import dev.goren98.brewerydelight.registry.ModComponents;
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

import java.util.Comparator;
import java.util.Optional;

public class AromaTableBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_DONOR = 0;
    public static final int SLOT_RECEIVER = 1;
    public static final int SIZE = 2;
    public static final int DEFAULT_PROCESS_TIME = 200;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private int processTime = DEFAULT_PROCESS_TIME;
    private final ContainerData data = new SimpleContainerData(2) {
        @Override public int get(int index) { return index == 0 ? progress : processTime; }
        @Override public void set(int index, int value) {
            if (index == 0) progress = value;
            else processTime = Math.max(1, value);
        }
    };

    public AromaTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AROMA_TABLE.get(), pos, state);
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.brewerydelight.aroma_table"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new AromaTableMenu(id, inventory, this, data); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return SIZE; }
    @Override public int getMaxStackSize() { return 1; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_DONOR -> AromaItems.hasAroma(stack);
            case SLOT_RECEIVER -> AromaItems.canReceiveAroma(stack);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        progress = 0;
        processTime = DEFAULT_PROCESS_TIME;
        super.setItem(slot, stack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.putInt("ProcessTime", processTime);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        processTime = tag.contains("ProcessTime") ? Math.max(1, tag.getInt("ProcessTime")) : DEFAULT_PROCESS_TIME;
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AromaTableBlockEntity table) {
        ItemStack donor = table.items.get(SLOT_DONOR);
        ItemStack receiver = table.items.get(SLOT_RECEIVER);
        if (donor.isEmpty() || receiver.isEmpty()
                || !AromaItems.hasAroma(donor) || !AromaItems.canReceiveAroma(receiver)) {
            table.resetProgress(level, pos, state);
            return;
        }

        Optional<String> donorAroma = AromaItems.currentAromaId(donor);
        Optional<String> receiverAroma = AromaItems.currentAromaId(receiver);
        if (donorAroma.isEmpty() || receiverAroma.isEmpty()) {
            table.resetProgress(level, pos, state);
            return;
        }

        Optional<RecipeHolder<AromaCombinationRecipe>> recipe = table.findRecipe(level, donor, receiver);
        String resultAroma = recipe.map(holder -> holder.value().resultAroma()).orElse(donorAroma.get());
        if (resultAroma.isBlank() || resultAroma.equals(receiverAroma.get())) {
            table.resetProgress(level, pos, state);
            return;
        }

        table.processTime = recipe.map(holder -> holder.value().processingTime()).orElse(DEFAULT_PROCESS_TIME);
        table.progress++;
        if (table.progress < table.processTime) return;

        donor.shrink(1);
        receiver.set(ModComponents.CROP_AROMA.get(), resultAroma);
        table.progress = 0;
        table.processTime = DEFAULT_PROCESS_TIME;
        setChanged(level, pos, state);
    }

    private Optional<RecipeHolder<AromaCombinationRecipe>> findRecipe(Level level, ItemStack donor, ItemStack receiver) {
        AromaCombinationInput input = new AromaCombinationInput(donor, receiver);
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.AROMA_COMBINATION_TYPE.get()).stream()
                .filter(holder -> holder.value().matches(input, level))
                .max(Comparator.comparingInt(holder -> holder.value().hasIngredientConstraint() ? 1 : 0));
    }

    private void resetProgress(Level level, BlockPos pos, BlockState state) {
        if (progress == 0 && processTime == DEFAULT_PROCESS_TIME) return;
        progress = 0;
        processTime = DEFAULT_PROCESS_TIME;
        setChanged(level, pos, state);
    }
}
