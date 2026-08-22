package dev.goren98.brewerydelight.brewing;

import dev.goren98.brewerydelight.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStationBlockEntity extends BaseContainerBlockEntity {
    public static final int SIZE = 6;
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public BrewingStationBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BREWING_STATION.get(), pos, state); }
    @Override protected Component getDefaultName() { return Component.translatable("container.brewerydelight.brewing_station"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new BrewingStationMenu(id, inventory, this); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return SIZE; }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); ContainerHelper.saveAllItems(tag, items, registries);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); items = NonNullList.withSize(SIZE, ItemStack.EMPTY); ContainerHelper.loadAllItems(tag, items, registries);
    }
}
