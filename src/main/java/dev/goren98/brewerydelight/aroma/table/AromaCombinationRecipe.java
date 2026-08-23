package dev.goren98.brewerydelight.aroma.table;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.goren98.brewerydelight.aroma.AromaItems;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Optional, directional Aroma Table combination. The ingredient, when present,
 * constrains the receiver item; otherwise only donor/receiver aroma IDs matter.
 */
public final class AromaCombinationRecipe implements Recipe<AromaCombinationInput> {
    public static final MapCodec<AromaCombinationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("donor_aroma").forGetter(AromaCombinationRecipe::donorAroma),
            Codec.STRING.fieldOf("receiver_aroma").forGetter(AromaCombinationRecipe::receiverAroma),
            Ingredient.CODEC_NONEMPTY.optionalFieldOf("ingredient").forGetter(AromaCombinationRecipe::ingredient),
            Codec.STRING.fieldOf("result_aroma").forGetter(AromaCombinationRecipe::resultAroma),
            Codec.INT.optionalFieldOf("processing_time", 200).forGetter(AromaCombinationRecipe::processingTime)
    ).apply(instance, AromaCombinationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AromaCombinationRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AromaCombinationRecipe decode(RegistryFriendlyByteBuf buf) {
            String donorAroma = ByteBufCodecs.STRING_UTF8.decode(buf);
            String receiverAroma = ByteBufCodecs.STRING_UTF8.decode(buf);
            Optional<Ingredient> ingredient = buf.readBoolean()
                    ? Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buf))
                    : Optional.empty();
            String resultAroma = ByteBufCodecs.STRING_UTF8.decode(buf);
            int processingTime = buf.readVarInt();
            return new AromaCombinationRecipe(donorAroma, receiverAroma, ingredient, resultAroma, processingTime);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AromaCombinationRecipe recipe) {
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.donorAroma);
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.receiverAroma);
            buf.writeBoolean(recipe.ingredient.isPresent());
            recipe.ingredient.ifPresent(value -> Ingredient.CONTENTS_STREAM_CODEC.encode(buf, value));
            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.resultAroma);
            buf.writeVarInt(recipe.processingTime);
        }
    };

    private final String donorAroma;
    private final String receiverAroma;
    private final Optional<Ingredient> ingredient;
    private final String resultAroma;
    private final int processingTime;

    public AromaCombinationRecipe(String donorAroma, String receiverAroma, Optional<Ingredient> ingredient,
                                  String resultAroma, int processingTime) {
        this.donorAroma = donorAroma;
        this.receiverAroma = receiverAroma;
        this.ingredient = ingredient;
        this.resultAroma = resultAroma;
        this.processingTime = Math.max(1, processingTime);
    }

    public String donorAroma() { return donorAroma; }
    public String receiverAroma() { return receiverAroma; }
    public Optional<Ingredient> ingredient() { return ingredient; }
    public String resultAroma() { return resultAroma; }
    public int processingTime() { return processingTime; }
    public boolean hasIngredientConstraint() { return ingredient.isPresent(); }

    @Override
    public boolean matches(AromaCombinationInput input, Level level) {
        Optional<String> donor = AromaItems.currentAromaId(input.donor());
        Optional<String> receiver = AromaItems.currentAromaId(input.receiver());
        if (donor.isEmpty() || receiver.isEmpty()) return false;
        if (!donorAroma.equals(donor.get()) || !receiverAroma.equals(receiver.get())) return false;
        return ingredient.isEmpty() || ingredient.get().test(input.receiver());
    }

    @Override
    public ItemStack assemble(AromaCombinationInput input, HolderLookup.Provider registries) {
        ItemStack result = input.receiver().copy();
        result.setCount(1);
        result.set(ModComponents.CROP_AROMA.get(), resultAroma);
        return result;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.AROMA_COMBINATION_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.AROMA_COMBINATION_TYPE.get(); }
}
