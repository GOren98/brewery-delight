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

    // 0 = base, 1 = finished brew, 2 = finished spirit
    public static final Supplier<DataComponentType<Integer>> STAGE = COMPONENTS.register("stage",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Integer>> AGE = COMPONENTS.register("age",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Long>> STARTED_AT = COMPONENTS.register("started_at",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());

    public static final Supplier<DataComponentType<String>> PRODUCT_ID = COMPONENTS.register("product_id",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<String>> PRIMARY_AROMA = COMPONENTS.register("primary_aroma",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<Integer>> PRIMARY_LEVEL = COMPONENTS.register("primary_level",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<String>> BARREL_AROMA = COMPONENTS.register("barrel_aroma",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<Integer>> BARREL_LEVEL = COMPONENTS.register("barrel_level",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Boolean>> SEASONING_COUNTED = COMPONENTS.register("seasoning_counted",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    private ModComponents() {}
}
