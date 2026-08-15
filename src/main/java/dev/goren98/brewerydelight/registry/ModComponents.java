package dev.goren98.brewerydelight.registry;

import com.mojang.serialization.Codec;
import dev.goren98.brewerydelight.BreweryDelight;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, BreweryDelight.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> STAGE = COMPONENTS.register("stage",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Integer>> AGE = COMPONENTS.register("age",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Long>> STARTED_AT = COMPONENTS.register("started_at",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());

    private ModComponents() {}
}
