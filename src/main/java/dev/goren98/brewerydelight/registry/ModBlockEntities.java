package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.cooking.CookingPotBlockEntity;
import dev.goren98.brewerydelight.crop.AromaCropBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BreweryDelight.MOD_ID);

    public static final Supplier<BlockEntityType<AromaCropBlockEntity>> AROMA_CROP = BLOCK_ENTITIES.register("aroma_crop",
            () -> BlockEntityType.Builder.of(AromaCropBlockEntity::new,
                    ModBlocks.CROPS.values().stream().map(Supplier::get).toArray(Block[]::new)).build(null));

    public static final Supplier<BlockEntityType<CookingPotBlockEntity>> COOKING_POT = BLOCK_ENTITIES.register("cooking_pot",
            () -> BlockEntityType.Builder.of(CookingPotBlockEntity::new, ModBlocks.COOKING_POT.get()).build(null));

    private ModBlockEntities() {}
}
