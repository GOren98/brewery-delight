package dev.goren98.brewerydelight.cooking.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.goren98.brewerydelight.aroma.AromaItems;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shapeless Brewing Pot recipe for the 0th-stage alcohol Base.
 * The recipe fixes the future Core direction; ingredient Aroma is carried by the produced Base.
 */
public final class BaseCookingRecipe implements Recipe<BaseCookingInput> {
    public static final MapCodec<BaseCookingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(BaseCookingRecipe::ingredients),
            Codec.STRING.fieldOf("base_id").forGetter(BaseCookingRecipe::baseId),
            Codec.STRING.fieldOf("display_name").forGetter(BaseCookingRecipe::displayName),
            Codec.INT.optionalFieldOf("color", 0xE6D7B9).forGetter(BaseCookingRecipe::color),
            Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(BaseCookingRecipe::cookingTime),
            Codec.INT.optionalFieldOf("servings", 4).forGetter(BaseCookingRecipe::servings),
            Codec.STRING.optionalFieldOf("brew_core", "").forGetter(BaseCookingRecipe::brewCore),
            Codec.STRING.optionalFieldOf("spirit_core", "").forGetter(BaseCookingRecipe::spiritCore)
    ).apply(instance, BaseCookingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BaseCookingRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BaseCookingRecipe decode(RegistryFriendlyByteBuf buf) {
            int ingredientCount = buf.readVarInt();
            java.util.ArrayList<Ingredient> ingredients = new java.util.ArrayList<>(ingredientCount);
            for (int i = 0; i < ingredientCount; i++) ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            String baseId = ByteBufCodecs.STRING_UTF8.decode(buf);
            String displayName = ByteBufCodecs.STRING_UTF8.decode(buf);
            int color = buf.readVarInt();
            int cookingTime = buf.readVarInt();
            int servings = buf.readVarInt();
            String brewCore = ByteBufCodecs.STRING_UTF8.decode(buf);
            String spiritCore = ByteBufCodecs.STRING_UTF8.decode(buf);
            return new BaseCookingRecipe(ingredients, baseId, displayName, color, cookingTime, servings, brewCore, spiritCore);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BaseCookingRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.baseId);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.displayName);
            buf.writeVarInt(recipe.color);
            buf.writeVarInt(recipe.cookingTime);
            buf.writeVarInt(recipe.servings);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.brewCore);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.spiritCore);
        }
    };

    private final List<Ingredient> ingredients;
    private final String baseId;
    private final String displayName;
    private final int color;
    private final int cookingTime;
    private final int servings;
    private final String brewCore;
    private final String spiritCore;

    public BaseCookingRecipe(List<Ingredient> ingredients, String baseId, String displayName, int color,
                             int cookingTime, int servings, String brewCore, String spiritCore) {
        this.ingredients = List.copyOf(ingredients);
        this.baseId = baseId;
        this.displayName = displayName;
        this.color = color;
        this.cookingTime = Math.max(1, cookingTime);
        this.servings = Math.max(1, servings);
        this.brewCore = brewCore;
        this.spiritCore = spiritCore;
    }

    public List<Ingredient> ingredients() { return ingredients; }
    public String baseId() { return baseId; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public int cookingTime() { return cookingTime; }
    public int servings() { return servings; }
    public String brewCore() { return brewCore; }
    public String spiritCore() { return spiritCore; }

    @Override
    public boolean matches(BaseCookingInput input, Level level) {
        if (input.size() != ingredients.size()) return false;
        boolean[] used = new boolean[input.size()];
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < input.size(); i++) {
                if (!used[i] && ingredient.test(input.getItem(i))) {
                    used[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(BaseCookingInput input, HolderLookup.Provider registries) {
        ItemStack result = new ItemStack(ModItems.BASE_BOTTLE.get(), servings);
        result.set(ModComponents.PRODUCT_ID.get(), baseId);
        result.set(ModComponents.DISPLAY_NAME.get(), displayName);
        result.set(ModComponents.STAGE.get(), 0);
        result.set(ModComponents.COLOR.get(), color);
        result.set(ModComponents.FERMENTABLE.get(), false);

        Map<String, Integer> aromaCounts = new HashMap<>();
        for (int i = 0; i < input.size(); i++) {
            AromaItems.currentAromaId(input.getItem(i)).ifPresent(aroma -> aromaCounts.merge(aroma, 1, Integer::sum));
        }
        aromaCounts.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .ifPresent(entry -> {
                    result.set(ModComponents.PRIMARY_AROMA.get(), entry.getKey());
                    result.set(ModComponents.PRIMARY_LEVEL.get(), entry.getValue());
                });
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack result = new ItemStack(ModItems.BASE_BOTTLE.get());
        result.set(ModComponents.PRODUCT_ID.get(), baseId);
        result.set(ModComponents.DISPLAY_NAME.get(), displayName);
        result.set(ModComponents.STAGE.get(), 0);
        result.set(ModComponents.COLOR.get(), color);
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> out = NonNullList.create();
        out.addAll(ingredients);
        return out;
    }

    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.BASE_COOKING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.BASE_COOKING_TYPE.get(); }
}
