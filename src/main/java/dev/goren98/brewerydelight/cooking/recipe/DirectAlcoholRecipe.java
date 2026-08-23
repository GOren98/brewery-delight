package dev.goren98.brewerydelight.cooking.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.alcohol.AlcoholStackFactory;
import dev.goren98.brewerydelight.registry.ModItems;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Data-driven Brewing Pot recipe that outputs a finished aroma-less alcohol directly. */
public final class DirectAlcoholRecipe implements Recipe<BaseCookingInput> {
    public static final MapCodec<DirectAlcoholRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(DirectAlcoholRecipe::ingredients),
            Codec.STRING.fieldOf("core_alcohol_id").forGetter(DirectAlcoholRecipe::coreAlcoholId),
            Codec.STRING.fieldOf("display_name").forGetter(DirectAlcoholRecipe::displayName),
            Codec.INT.optionalFieldOf("color", 0xFFFFFF).forGetter(DirectAlcoholRecipe::color),
            Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(DirectAlcoholRecipe::cookingTime)
    ).apply(i, DirectAlcoholRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DirectAlcoholRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override public DirectAlcoholRecipe decode(RegistryFriendlyByteBuf b) {
            int n=b.readVarInt(); List<Ingredient> ingredients=new ArrayList<>(n);
            for(int x=0;x<n;x++) ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(b));
            return new DirectAlcoholRecipe(ingredients, ByteBufCodecs.STRING_UTF8.decode(b),
                    ByteBufCodecs.STRING_UTF8.decode(b), b.readVarInt(), b.readVarInt());
        }
        @Override public void encode(RegistryFriendlyByteBuf b, DirectAlcoholRecipe r) {
            b.writeVarInt(r.ingredients.size());
            r.ingredients.forEach(x -> Ingredient.CONTENTS_STREAM_CODEC.encode(b,x));
            ByteBufCodecs.STRING_UTF8.encode(b,r.coreAlcoholId);
            ByteBufCodecs.STRING_UTF8.encode(b,r.displayName);
            b.writeVarInt(r.color); b.writeVarInt(r.cookingTime);
        }
    };

    private final List<Ingredient> ingredients; private final String coreAlcoholId, displayName;
    private final int color, cookingTime;
    public DirectAlcoholRecipe(List<Ingredient> ingredients,String coreAlcoholId,String displayName,int color,int cookingTime){
        this.ingredients=List.copyOf(ingredients); this.coreAlcoholId=coreAlcoholId; this.displayName=displayName;
        this.color=color; this.cookingTime=Math.max(1,cookingTime);
    }
    public List<Ingredient> ingredients(){return ingredients;} public String coreAlcoholId(){return coreAlcoholId;}
    public String displayName(){return displayName;} public int color(){return color;} public int cookingTime(){return cookingTime;}
    @Override public boolean matches(BaseCookingInput input, Level level){return shapeless(input,ingredients);}
    static boolean shapeless(BaseCookingInput input,List<Ingredient> ingredients){
        if(input.size()!=ingredients.size()) return false; boolean[] used=new boolean[input.size()];
        for(Ingredient ingredient:ingredients){boolean found=false; for(int x=0;x<input.size();x++) if(!used[x]&&ingredient.test(input.getItem(x))){used[x]=true;found=true;break;} if(!found)return false;}
        return true;
    }
    @Override public ItemStack assemble(BaseCookingInput input,HolderLookup.Provider registries){
        return AlcoholStackFactory.create(ModItems.SPIRIT_BOTTLE.get(), coreAlcoholId, displayName,
                2, color, "", 0, Map.of());
    }
    @Override public boolean canCraftInDimensions(int w,int h){return w*h>=ingredients.size();}
    @Override public ItemStack getResultItem(HolderLookup.Provider r){return new ItemStack(ModItems.SPIRIT_BOTTLE.get());}
    @Override public boolean isSpecial(){return true;} @Override public RecipeSerializer<?> getSerializer(){return ModRecipes.DIRECT_ALCOHOL_SERIALIZER.get();}
    @Override public RecipeType<?> getType(){return ModRecipes.DIRECT_ALCOHOL_TYPE.get();}
}
