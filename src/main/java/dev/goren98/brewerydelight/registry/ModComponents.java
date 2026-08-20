package dev.goren98.brewerydelight.registry;

import com.mojang.serialization.Codec;
import dev.goren98.brewerydelight.BreweryDelight;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.function.Supplier;

public final class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, BreweryDelight.MOD_ID);

    public static final Supplier<DataComponentType<String>> CROP_AROMA = COMPONENTS.register("crop_aroma",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<Integer>> STAGE = COMPONENTS.register("stage",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Integer>> AGE = COMPONENTS.register("age",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Long>> STARTED_AT = COMPONENTS.register("started_at",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final Supplier<DataComponentType<String>> PRODUCT_ID = COMPONENTS.register("product_id",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<String>> DISPLAY_NAME = COMPONENTS.register("display_name",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<String>> DISTILL_PRODUCT_ID = COMPONENTS.register("distill_product_id",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<String>> DISTILL_DISPLAY_NAME = COMPONENTS.register("distill_display_name",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<Boolean>> FERMENTABLE = COMPONENTS.register("fermentable",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
    public static final Supplier<DataComponentType<Integer>> COLOR = COMPONENTS.register("color",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Integer>> DISTILL_COLOR = COMPONENTS.register("distill_color",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<String>> PRIMARY_AROMA = COMPONENTS.register("primary_aroma",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<Integer>> PRIMARY_LEVEL = COMPONENTS.register("primary_level",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<String>> BARREL_AROMA = COMPONENTS.register("barrel_aroma",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final Supplier<DataComponentType<Integer>> BARREL_LEVEL = COMPONENTS.register("barrel_level",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final Supplier<DataComponentType<Map<String, Integer>>> AGING_AROMAS = COMPONENTS.register("aging_aromas",
            () -> DataComponentType.<Map<String, Integer>>builder().persistent(Codec.unboundedMap(Codec.STRING, Codec.INT)).build());
    public static final Supplier<DataComponentType<Map<String, Integer>>> BLEND_AROMAS = COMPONENTS.register("blend_aromas",
            () -> DataComponentType.<Map<String, Integer>>builder().persistent(Codec.unboundedMap(Codec.STRING, Codec.INT)).build());
    public static final Supplier<DataComponentType<Boolean>> SEASONING_COUNTED = COMPONENTS.register("seasoning_counted",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

    private ModComponents() {}
}
