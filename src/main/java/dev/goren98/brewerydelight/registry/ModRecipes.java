package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.alcohol.CoreAlcoholRecipe;
import dev.goren98.brewerydelight.alcohol.AlcoholTransformationRecipe;
import dev.goren98.brewerydelight.aroma.table.AromaCombinationRecipe;
import dev.goren98.brewerydelight.cooking.recipe.BaseCookingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, BreweryDelight.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, BreweryDelight.MOD_ID);

    public static final Supplier<RecipeType<BaseCookingRecipe>> BASE_COOKING_TYPE = RECIPE_TYPES.register(
            "base_cooking", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "base_cooking")));
    public static final Supplier<RecipeSerializer<BaseCookingRecipe>> BASE_COOKING_SERIALIZER = RECIPE_SERIALIZERS.register(
            "base_cooking", () -> new RecipeSerializer<>() {
                @Override public com.mojang.serialization.MapCodec<BaseCookingRecipe> codec() { return BaseCookingRecipe.CODEC; }
                @Override public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, BaseCookingRecipe> streamCodec() { return BaseCookingRecipe.STREAM_CODEC; }
            });

    public static final Supplier<RecipeType<CoreAlcoholRecipe>> CORE_ALCOHOL_TYPE = RECIPE_TYPES.register(
            "core_alcohol", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "core_alcohol")));
    public static final Supplier<RecipeSerializer<CoreAlcoholRecipe>> CORE_ALCOHOL_SERIALIZER = RECIPE_SERIALIZERS.register(
            "core_alcohol", () -> new RecipeSerializer<>() {
                @Override public com.mojang.serialization.MapCodec<CoreAlcoholRecipe> codec() { return CoreAlcoholRecipe.CODEC; }
                @Override public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CoreAlcoholRecipe> streamCodec() { return CoreAlcoholRecipe.STREAM_CODEC; }
            });

    public static final Supplier<RecipeType<AromaCombinationRecipe>> AROMA_COMBINATION_TYPE = RECIPE_TYPES.register(
            "aroma_combination", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "aroma_combination")));
    public static final Supplier<RecipeSerializer<AromaCombinationRecipe>> AROMA_COMBINATION_SERIALIZER = RECIPE_SERIALIZERS.register(
            "aroma_combination", () -> new RecipeSerializer<>() {
                @Override public com.mojang.serialization.MapCodec<AromaCombinationRecipe> codec() { return AromaCombinationRecipe.CODEC; }
                @Override public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AromaCombinationRecipe> streamCodec() { return AromaCombinationRecipe.STREAM_CODEC; }
            });

    public static final Supplier<RecipeType<AlcoholTransformationRecipe>> ALCOHOL_TRANSFORMATION_TYPE = RECIPE_TYPES.register(
            "alcohol_transformation", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "alcohol_transformation")));
    public static final Supplier<RecipeSerializer<AlcoholTransformationRecipe>> ALCOHOL_TRANSFORMATION_SERIALIZER = RECIPE_SERIALIZERS.register(
            "alcohol_transformation", () -> new RecipeSerializer<>() {
                @Override public com.mojang.serialization.MapCodec<AlcoholTransformationRecipe> codec() { return AlcoholTransformationRecipe.CODEC; }
                @Override public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AlcoholTransformationRecipe> streamCodec() { return AlcoholTransformationRecipe.STREAM_CODEC; }
            });

    private ModRecipes() {}
}
