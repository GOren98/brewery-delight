package dev.goren98.brewerydelight.cooking.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.goren98.brewerydelight.alcohol.AlcoholTransformationResolver;
import dev.goren98.brewerydelight.aroma.AromaItems;
import dev.goren98.brewerydelight.item.AromaUtil;
import dev.goren98.brewerydelight.registry.ModComponents;
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

import java.util.*;

/** Unaged Brew/Spirit + four new materials -> a new Liqueur Core. */
public final class LiqueurCookingRecipe implements Recipe<BaseCookingInput> {
    public static final MapCodec<LiqueurCookingRecipe> CODEC= RecordCodecBuilder.mapCodec(i->i.group(
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(LiqueurCookingRecipe::ingredients),
            Codec.STRING.optionalFieldOf("required_alcohol_type","any").forGetter(LiqueurCookingRecipe::requiredAlcoholType),
            Codec.STRING.optionalFieldOf("required_core","").forGetter(LiqueurCookingRecipe::requiredCore),
            Codec.STRING.fieldOf("core_alcohol_id").forGetter(LiqueurCookingRecipe::coreAlcoholId),
            Codec.STRING.fieldOf("display_name").forGetter(LiqueurCookingRecipe::displayName),
            Codec.INT.optionalFieldOf("color",0xFFFFFF).forGetter(LiqueurCookingRecipe::color),
            Codec.INT.optionalFieldOf("cookingtime",100).forGetter(LiqueurCookingRecipe::cookingTime)
    ).apply(i,LiqueurCookingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,LiqueurCookingRecipe> STREAM_CODEC=new StreamCodec<>(){
        @Override public LiqueurCookingRecipe decode(RegistryFriendlyByteBuf b){int n=b.readVarInt();List<Ingredient> x=new ArrayList<>();for(int j=0;j<n;j++)x.add(Ingredient.CONTENTS_STREAM_CODEC.decode(b));return new LiqueurCookingRecipe(x,ByteBufCodecs.STRING_UTF8.decode(b),ByteBufCodecs.STRING_UTF8.decode(b),ByteBufCodecs.STRING_UTF8.decode(b),ByteBufCodecs.STRING_UTF8.decode(b),b.readVarInt(),b.readVarInt());}
        @Override public void encode(RegistryFriendlyByteBuf b,LiqueurCookingRecipe r){b.writeVarInt(r.ingredients.size());r.ingredients.forEach(x->Ingredient.CONTENTS_STREAM_CODEC.encode(b,x));ByteBufCodecs.STRING_UTF8.encode(b,r.requiredAlcoholType);ByteBufCodecs.STRING_UTF8.encode(b,r.requiredCore);ByteBufCodecs.STRING_UTF8.encode(b,r.coreAlcoholId);ByteBufCodecs.STRING_UTF8.encode(b,r.displayName);b.writeVarInt(r.color);b.writeVarInt(r.cookingTime);}
    };
    private final List<Ingredient> ingredients; private final String requiredAlcoholType,requiredCore,coreAlcoholId,displayName; private final int color,cookingTime;
    public LiqueurCookingRecipe(List<Ingredient> i,String t,String rc,String c,String d,int color,int time){ingredients=List.copyOf(i);requiredAlcoholType=t;requiredCore=rc;coreAlcoholId=c;displayName=d;this.color=color;cookingTime=Math.max(1,time);}
    public List<Ingredient> ingredients(){return ingredients;} public String requiredAlcoholType(){return requiredAlcoholType;} public String requiredCore(){return requiredCore;} public String coreAlcoholId(){return coreAlcoholId;} public String displayName(){return displayName;} public int color(){return color;} public int cookingTime(){return cookingTime;}
    private ItemStack alcohol(BaseCookingInput input){for(int i=0;i<input.size();i++){ItemStack s=input.getItem(i);int stage=s.getOrDefault(ModComponents.STAGE.get(),0);if(stage==1||stage==2)return s;}return ItemStack.EMPTY;}
    private List<ItemStack> materials(BaseCookingInput input,ItemStack alcohol){List<ItemStack> out=new ArrayList<>();for(int i=0;i<input.size();i++)if(input.getItem(i)!=alcohol)out.add(input.getItem(i));return out;}
    @Override public boolean matches(BaseCookingInput input,Level level){ItemStack a=alcohol(input);if(a.isEmpty()||input.size()!=ingredients.size()+1)return false;if(a.getOrDefault(ModComponents.AGE.get(),0)!=0||a.getOrDefault(ModComponents.BARREL_LEVEL.get(),0)!=0||!a.getOrDefault(ModComponents.BLEND_AROMAS.get(),Map.<String,Integer>of()).isEmpty())return false;int stage=a.getOrDefault(ModComponents.STAGE.get(),0);if((requiredAlcoholType.equals("brew")&&stage!=1)||(requiredAlcoholType.equals("spirit")&&stage!=2))return false;if(!requiredCore.isEmpty()&&!requiredCore.equals(a.getOrDefault(ModComponents.CORE_ALCOHOL_ID.get(),"")))return false;return DirectAlcoholRecipe.shapeless(new BaseCookingInput(materials(input,a)),ingredients);}
    @Override public ItemStack assemble(BaseCookingInput input,HolderLookup.Provider registries){ItemStack a=alcohol(input);if(a.isEmpty())return ItemStack.EMPTY;List<ItemStack> materials=materials(input,a);Map<String,Integer> counts=new HashMap<>();materials.forEach(s->AromaItems.currentAromaId(s).ifPresent(x->counts.merge(x,1,Integer::sum)));Map.Entry<String,Integer> primary=counts.entrySet().stream().max(Map.Entry.<String,Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())).orElse(null);ItemStack out=new ItemStack(ModItems.LIQUEUR_BOTTLE.get());out.set(ModComponents.CORE_ALCOHOL_ID.get(),coreAlcoholId);out.set(ModComponents.PRODUCT_ID.get(),coreAlcoholId);out.set(ModComponents.DISPLAY_NAME.get(),displayName);out.set(ModComponents.STAGE.get(),3);out.set(ModComponents.AGE.get(),0);out.set(ModComponents.COLOR.get(),color);if(primary!=null){out.set(ModComponents.PRIMARY_AROMA.get(),primary.getKey());out.set(ModComponents.PRIMARY_LEVEL.get(),Math.min(5,primary.getValue()+1));}else{out.set(ModComponents.PRIMARY_AROMA.get(),"");out.set(ModComponents.PRIMARY_LEVEL.get(),0);}out.set(ModComponents.INHERITED_AROMAS.get(),Map.copyOf(AromaUtil.merged(a)));out.set(ModComponents.BARREL_LEVEL.get(),0);out.set(ModComponents.AGING_AROMAS.get(),Map.of());out.set(ModComponents.BLEND_AROMAS.get(),Map.of());out.set(ModComponents.SEASONING_COUNTED.get(),false);return out;}
    @Override public boolean canCraftInDimensions(int w,int h){return w*h>=ingredients.size()+1;} @Override public ItemStack getResultItem(HolderLookup.Provider p){return new ItemStack(ModItems.LIQUEUR_BOTTLE.get());}@Override public boolean isSpecial(){return true;}@Override public RecipeSerializer<?> getSerializer(){return ModRecipes.LIQUEUR_COOKING_SERIALIZER.get();}@Override public RecipeType<?> getType(){return ModRecipes.LIQUEUR_COOKING_TYPE.get();}
}
