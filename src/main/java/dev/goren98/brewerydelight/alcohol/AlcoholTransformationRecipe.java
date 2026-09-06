package dev.goren98.brewerydelight.alcohol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.goren98.brewerydelight.item.AromaUtil;
import dev.goren98.brewerydelight.aroma.AromaDefinitions;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data-driven Core Alcohol -> Higher Alcohol identity transformation.
 * Required aromas are minimum levels; aromas not listed by the recipe are allowed.
 */
public final class AlcoholTransformationRecipe implements Recipe<SingleRecipeInput> {
    private static final Codec<Map<String, Integer>> AROMAS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT);

    public static final MapCodec<AlcoholTransformationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("core_alcohol_id").forGetter(AlcoholTransformationRecipe::coreAlcoholId),
            AROMAS_CODEC.fieldOf("required_aromas").forGetter(AlcoholTransformationRecipe::requiredAromas),
            AROMAS_CODEC.optionalFieldOf("required_ingredient_families", Map.of())
                    .forGetter(AlcoholTransformationRecipe::requiredIngredientFamilies),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(AlcoholTransformationRecipe::priority),
            Result.CODEC.fieldOf("result").forGetter(AlcoholTransformationRecipe::result)
    ).apply(instance, AlcoholTransformationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlcoholTransformationRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AlcoholTransformationRecipe decode(RegistryFriendlyByteBuf buf) {
            String coreAlcoholId = ByteBufCodecs.STRING_UTF8.decode(buf);
            int aromaCount = buf.readVarInt();
            Map<String, Integer> aromas = new LinkedHashMap<>();
            for (int i = 0; i < aromaCount; i++) {
                aromas.put(ByteBufCodecs.STRING_UTF8.decode(buf), buf.readVarInt());
            }
            int familyCount = buf.readVarInt();
            Map<String, Integer> families = new LinkedHashMap<>();
            for (int i = 0; i < familyCount; i++) {
                families.put(ByteBufCodecs.STRING_UTF8.decode(buf), buf.readVarInt());
            }
            int priority = buf.readVarInt();
            return new AlcoholTransformationRecipe(coreAlcoholId, aromas, families, priority, Result.STREAM_CODEC.decode(buf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AlcoholTransformationRecipe recipe) {
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.coreAlcoholId);
            buf.writeVarInt(recipe.requiredAromas.size());
            recipe.requiredAromas.forEach((aroma, level) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, aroma);
                buf.writeVarInt(level);
            });
            buf.writeVarInt(recipe.requiredIngredientFamilies.size());
            recipe.requiredIngredientFamilies.forEach((family, level) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, family);
                buf.writeVarInt(level);
            });
            buf.writeVarInt(recipe.priority);
            Result.STREAM_CODEC.encode(buf, recipe.result);
        }
    };

    private final String coreAlcoholId;
    private final Map<String, Integer> requiredAromas;
    private final Map<String, Integer> requiredIngredientFamilies;
    private final int priority;
    private final Result result;

    public AlcoholTransformationRecipe(String coreAlcoholId, Map<String, Integer> requiredAromas,
                                       Map<String, Integer> requiredIngredientFamilies, int priority, Result result) {
        this.coreAlcoholId = coreAlcoholId;
        this.requiredAromas = Map.copyOf(requiredAromas);
        this.requiredIngredientFamilies = Map.copyOf(requiredIngredientFamilies);
        this.priority = priority;
        this.result = result;
    }

    public String coreAlcoholId() { return coreAlcoholId; }
    public Map<String, Integer> requiredAromas() { return requiredAromas; }
    public Map<String, Integer> requiredIngredientFamilies() { return requiredIngredientFamilies; }
    public int priority() { return priority; }
    public Result result() { return result; }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.item();
        if (!coreAlcoholId.equals(stack.getOrDefault(ModComponents.CORE_ALCOHOL_ID.get(), ""))) return false;
        Map<String, Integer> aromas = AromaUtil.merged(stack);
        if (!requiredAromas.entrySet().stream().allMatch(entry ->
                entry.getValue() > 0 && aromas.getOrDefault(entry.getKey(), 0) >= entry.getValue())) return false;

        Map<String, Integer> familyLevels = new LinkedHashMap<>();
        aromas.forEach((aroma, aromaLevel) -> {
            String family = AromaDefinitions.ingredientFamily(aroma);
            if (!family.isBlank()) familyLevels.merge(family, aromaLevel, Integer::sum);
        });
        return requiredIngredientFamilies.entrySet().stream().allMatch(entry ->
                entry.getValue() > 0 && familyLevels.getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    public void applyTo(ItemStack stack) {
        stack.set(ModComponents.PRODUCT_ID.get(), result.productId());
        if (!result.displayName().isEmpty()) stack.set(ModComponents.DISPLAY_NAME.get(), result.displayName());
        result.color().ifPresent(color -> stack.set(ModComponents.COLOR.get(), color));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack resultStack = input.item().copy();
        applyTo(resultStack);
        return resultStack;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.ALCOHOL_TRANSFORMATION_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.ALCOHOL_TRANSFORMATION_TYPE.get(); }

    public record Result(String productId, String displayName, java.util.Optional<Integer> color) {
        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("product_id").forGetter(Result::productId),
                Codec.STRING.optionalFieldOf("display_name", "").forGetter(Result::displayName),
                Codec.INT.optionalFieldOf("color").forGetter(Result::color)
        ).apply(instance, Result::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Result> STREAM_CODEC = new StreamCodec<>() {
            @Override public Result decode(RegistryFriendlyByteBuf buf) {
                String productId = ByteBufCodecs.STRING_UTF8.decode(buf);
                String displayName = ByteBufCodecs.STRING_UTF8.decode(buf);
                return new Result(productId, displayName, buf.readBoolean()
                        ? java.util.Optional.of(buf.readVarInt()) : java.util.Optional.empty());
            }

            @Override public void encode(RegistryFriendlyByteBuf buf, Result result) {
                ByteBufCodecs.STRING_UTF8.encode(buf, result.productId);
                ByteBufCodecs.STRING_UTF8.encode(buf, result.displayName);
                buf.writeBoolean(result.color.isPresent());
                result.color.ifPresent(buf::writeVarInt);
            }
        };
    }
}
