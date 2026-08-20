package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AromaCropBlockEntity extends BlockEntity {
    private String aroma = "";

    public AromaCropBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AROMA_CROP.get(), pos, state);
    }

    public String getAroma(String fallback) {
        return aroma == null || aroma.isBlank() ? fallback : aroma;
    }

    public void setAroma(String aroma) {
        this.aroma = aroma == null ? "" : aroma;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (aroma != null && !aroma.isBlank()) tag.putString("Aroma", aroma);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        aroma = tag.getString("Aroma");
    }
}
