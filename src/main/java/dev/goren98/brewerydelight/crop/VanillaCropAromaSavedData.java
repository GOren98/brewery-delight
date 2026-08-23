package dev.goren98.brewerydelight.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Stores only non-default aromas attached to vanilla crop positions. */
public final class VanillaCropAromaSavedData extends SavedData {
    private static final String NAME = "brewerydelight_vanilla_crop_aromas";
    private final Map<Long, String> aromas = new HashMap<>();

    public static VanillaCropAromaSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(VanillaCropAromaSavedData::new, VanillaCropAromaSavedData::load), NAME);
    }

    public Optional<String> get(BlockPos pos) { return Optional.ofNullable(aromas.get(pos.asLong())); }

    public void put(BlockPos pos, String aroma, String defaultAroma) {
        if (aroma == null || aroma.isBlank() || aroma.equals(defaultAroma)) {
            remove(pos);
            return;
        }
        String previous = aromas.put(pos.asLong(), aroma);
        if (!aroma.equals(previous)) setDirty();
    }

    public void remove(BlockPos pos) {
        if (aromas.remove(pos.asLong()) != null) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag entries = new ListTag();
        aromas.forEach((pos, aroma) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", pos);
            entry.putString("Aroma", aroma);
            entries.add(entry);
        });
        tag.put("Entries", entries);
        return tag;
    }

    private static VanillaCropAromaSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        VanillaCropAromaSavedData data = new VanillaCropAromaSavedData();
        ListTag entries = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            String aroma = entry.getString("Aroma");
            if (!aroma.isBlank()) data.aromas.put(entry.getLong("Pos"), aroma);
        }
        return data;
    }
}
