package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.cooking.CookingPotBlock;
import dev.goren98.brewerydelight.crop.AromaCropBlock;
import dev.goren98.brewerydelight.crop.AromaCropBlock.GrowthStyle;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, BreweryDelight.MOD_ID);
    public static final Map<String, Supplier<AromaCropBlock>> CROPS = new LinkedHashMap<>();

    private static Supplier<AromaCropBlock> crop(String id, GrowthStyle style) {
        Supplier<AromaCropBlock> block = BLOCKS.register(id + "_crop", () -> new AromaCropBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noCollission().randomTicks(), id, id, style));
        CROPS.put(id, block);
        return block;
    }

    public static final Supplier<AromaCropBlock> RED_GRAPE = crop("red_grape", GrowthStyle.TALL);
    public static final Supplier<AromaCropBlock> WHITE_GRAPE = crop("white_grape", GrowthStyle.TALL);
    public static final Supplier<AromaCropBlock> BERRY = crop("berry", GrowthStyle.BUSH);
    public static final Supplier<AromaCropBlock> APPLE = crop("apple", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> PEAR = crop("pear", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> PEACH = crop("peach", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> ORANGE = crop("orange", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> LEMON = crop("lemon", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> MANGO = crop("mango", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> KIWI = crop("kiwi", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> PINEAPPLE = crop("pineapple", GrowthStyle.BUSH);
    public static final Supplier<AromaCropBlock> CHERRY = crop("cherry", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> PLUM = crop("plum", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> APRICOT = crop("apricot", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> LIME = crop("lime", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> BANANA = crop("banana", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> RICE = crop("rice", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> BARLEY = crop("barley", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> CORN = crop("corn", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> SWEET_POTATO = crop("sweet_potato", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> HOPS = crop("hops", GrowthStyle.TALL);
    public static final Supplier<AromaCropBlock> TEA = crop("tea", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> RYE = crop("rye", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> AGAVE = crop("agave", GrowthStyle.BUSH);
    public static final Supplier<AromaCropBlock> JUNIPER = crop("juniper", GrowthStyle.BUSH);
    public static final Supplier<AromaCropBlock> MILLET = crop("millet", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> ANISE = crop("anise", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> ALMOND = crop("almond", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> WALNUT = crop("walnut", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> COFFEE = crop("coffee", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> VANILLA = crop("vanilla", GrowthStyle.TALL);
    public static final Supplier<AromaCropBlock> CINNAMON = crop("cinnamon", GrowthStyle.TREE);
    public static final Supplier<AromaCropBlock> GINGER = crop("ginger", GrowthStyle.FIELD);
    public static final Supplier<AromaCropBlock> NUTMEG = crop("nutmeg", GrowthStyle.TREE);

    public static final Supplier<Block> COOKING_POT = BLOCKS.register("cooking_pot",
            () -> new CookingPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.0F)));

    private ModBlocks() {}
}
