package dev.goren98.brewerydelight.alcohol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Data-driven Base -> Core Alcohol conversion.
 * Adding a new Core Alcohol only requires another recipe JSON using this type.
 */
public final class CoreAlcoholRecipe implements Recipe<CoreAlcoholInput> {
    public static final MapCodec<CoreAlcoholRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("process").forGetter(CoreAlcoholRecipe::process),
            Codec.STRING.fieldOf("base_id").forGetter(CoreAlcoholRecipe::baseId),
            Codec.STRING.fieldOf("core_alcohol_id").forGetter(CoreAlcoholRecipe::coreAlcoholId),
            Codec.STRING.fieldOf("display_name").forGetter(CoreAlcoholRecipe::displayName),
            Codec.INT.optionalFieldOf("color", 0xFFFFFF).forGetter(CoreAlcoholRecipe::color),
            Codec.INT.optionalFieldOf("aroma_level_bonus", 1).forGetter(CoreAlcoholRecipe::aromaLevelBonus),
            Codec.INT.optionalFieldOf("processing_time", 200).forGetter(CoreAlcoholRecipe::processingTime)
    ).apply(instance, CoreAlcoholRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoreAlcoholRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override public CoreAlcoholRecipe decode(RegistryFriendlyByteBuf buf) {
            return new CoreAlcoholRecipe(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override public void encode(RegistryFriendlyByteBuf buf, CoreAlcoholRecipe recipe) {
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.process);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.baseId);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.coreAlcoholId);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.displayName);
            buf.writeVarInt(recipe.color);
            buf.writeVarInt(recipe.aromaLevelBonus);
            buf.writeVarInt(recipe.processingTime);
        }
    };

    private final String process;
    private final String baseId;
    private final String coreAlcoholId;
    private final String displayName;
    private final int color;
    private final int aromaLevelBonus;
    private final int processingTime;

    public CoreAlcoholRecipe(String process, String baseId, String coreAlcoholId, String displayName,
                             int color, int aromaLevelBonus, int processingTime) {
        this.process = process;
        this.baseId = baseId;
        this.coreAlcoholId = coreAlcoholId;
        this.displayName = displayName;
        this.color = color;
        this.aromaLevelBonus = aromaLevelBonus;
        this.processingTime = Math.max(1, processingTime);
    }

    public String process() { return process; }
    public String baseId() { return baseId; }
    public String coreAlcoholId() { return coreAlcoholId; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public int aromaLevelBonus() { return aromaLevelBonus; }
    public int processingTime() { return processingTime; }

    @Override
    public boolean matches(CoreAlcoholInput input, Level level) {
        if (!process.equals(input.process()) || !input.base().is(ModItems.BASE_BOTTLE.get())) return false;
        return baseId.equals(input.base().getOrDefault(ModComponents.PRODUCT_ID.get(), ""));
    }

    @Override
    public ItemStack assemble(CoreAlcoholInput input, HolderLookup.Provider registries) {
        ItemStack base = input.base();
        boolean spirit = "spirit".equals(process);
        ItemStack result = new ItemStack(spirit ? ModItems.SPIRIT_BOTTLE.get() : ModItems.BREW_BOTTLE.get());

        String aroma = base.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int aromaLevel = base.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        int nextLevel = aromaLevel <= 0 ? 0 : Math.min(5, aromaLevel + aromaLevelBonus);

        result.set(ModComponents.CORE_ALCOHOL_ID.get(), coreAlcoholId);
        // PRODUCT_ID is kept in sync during MVP migration because Barrel/Blend legacy code still keys by it.
        result.set(ModComponents.PRODUCT_ID.get(), coreAlcoholId);
        result.set(ModComponents.DISPLAY_NAME.get(), displayName);
        result.set(ModComponents.STAGE.get(), spirit ? 2 : 1);
        result.set(ModComponents.AGE.get(), 0);
        result.set(ModComponents.PRIMARY_AROMA.get(), aroma);
        result.set(ModComponents.PRIMARY_LEVEL.get(), nextLevel);
        result.set(ModComponents.COLOR.get(), color);
        result.set(ModComponents.FERMENTABLE.get(), false);
        result.set(ModComponents.BARREL_LEVEL.get(), 0);
        result.set(ModComponents.AGING_AROMAS.get(), Map.of());
        result.set(ModComponents.BLEND_AROMAS.get(), Map.of());
        result.set(ModComponents.SEASONING_COUNTED.get(), false);
        return result;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        boolean spirit = "spirit".equals(process);
        ItemStack result = new ItemStack(spirit ? ModItems.SPIRIT_BOTTLE.get() : ModItems.BREW_BOTTLE.get());
        result.set(ModComponents.CORE_ALCOHOL_ID.get(), coreAlcoholId);
        result.set(ModComponents.PRODUCT_ID.get(), coreAlcoholId);
        result.set(ModComponents.DISPLAY_NAME.get(), displayName);
        result.set(ModComponents.STAGE.get(), spirit ? 2 : 1);
        result.set(ModComponents.COLOR.get(), color);
        return result;
    }

    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CORE_ALCOHOL_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.CORE_ALCOHOL_TYPE.get(); }
}
