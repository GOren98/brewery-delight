package dev.goren98.brewerydelight.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public final class BarrelSavedData extends SavedData {
    private static final String NAME = "brewerydelight_barrels";
    private final Map<Long, BarrelInventory> barrels = new HashMap<>();

    public static BarrelSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BarrelSavedData::new, BarrelSavedData::load), NAME);
    }

    public BarrelInventory getOrCreate(BlockPos controller) {
        return barrels.computeIfAbsent(controller.asLong(), p -> new BarrelInventory(this));
    }

    public void remove(BlockPos controller) {
        if (barrels.remove(controller.asLong()) != null) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        barrels.forEach((pos, inv) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", pos);
            ListTag items = new ListTag();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    CompoundTag item = new CompoundTag();
                    item.putByte("Slot", (byte)i);
                    item.put("Stack", stack.save(provider));
                    items.add(item);
                }
            }
            entry.put("Items", items);
            list.add(entry);
        });
        tag.put("Barrels", list);
        return tag;
    }

    private static BarrelSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BarrelSavedData data = new BarrelSavedData();
        ListTag list = tag.getList("Barrels", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BarrelInventory inv = new BarrelInventory(data);
            ListTag items = entry.getList("Items", Tag.TAG_COMPOUND);
            for (int j = 0; j < items.size(); j++) {
                CompoundTag item = items.getCompound(j);
                ItemStack.parse(provider, item.getCompound("Stack")).ifPresent(s -> inv.setItemSilently(item.getByte("Slot") & 255, s));
            }
            data.barrels.put(entry.getLong("Pos"), inv);
        }
        return data;
    }
}
