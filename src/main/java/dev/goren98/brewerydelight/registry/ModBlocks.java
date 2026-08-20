package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.cooking.CookingPotBlock;
import dev.goren98.brewerydelight.crop.AromaCropBlock;
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

    private static Supplier<AromaCropBlock> crop(String id, boolean orchard) {
        Supplier<AromaCropBlock> block = BLOCKS.register(id + "_crop", () -> new AromaCropBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noCollission().randomTicks(), id, id, orchard));
        CROPS.put(id, block);
        return block;
    }

    public static final Supplier<AromaCropBlock> RED_GRAPE = crop("red_grape", true);
    public static final Supplier<AromaCropBlock> WHITE_GRAPE = crop("white_grape", true);
    public static final Supplier<AromaCropBlock> BERRY = crop("berry", true);
    public static final Supplier<AromaCropBlock> APPLE = crop("apple", true);
    public static final Supplier<AromaCropBlock> PEAR = crop("pear", true);
    public static final Supplier<AromaCropBlock> PEACH = crop("peach", true);
    public static final Supplier<AromaCropBlock> ORANGE = crop("orange", true);
    public static final Supplier<AromaCropBlock> LEMON = crop("lemon", true);
    public static final Supplier<AromaCropBlock> MANGO = crop("mango", true);
    public static final Supplier<AromaCropBlock> KIWI = crop("kiwi", true);
    public static final Supplier<AromaCropBlock> PINEAPPLE = crop("pineapple", false);
    public static final Supplier<AromaCropBlock> CHERRY = crop("cherry", true);
    public static final Supplier<AromaCropBlock> PLUM = crop("plum", true);
    public static final Supplier<AromaCropBlock> APRICOT = crop("apricot", true);
    public static final Supplier<AromaCropBlock> LIME = crop("lime", true);
    public static final Supplier<AromaCropBlock> BANANA = crop("banana", true);
    public static final Supplier<AromaCropBlock> RICE = crop("rice", false);
    public static final Supplier<AromaCropBlock> BARLEY = crop("barley", false);
    public static final Supplier<AromaCropBlock> CORN = crop("corn", false);
    public static final Supplier<AromaCropBlock> SWEET_POTATO = crop("sweet_potato", false);
    public static final Supplier<AromaCropBlock> HOPS = crop("hops", false);
    public static final Supplier<AromaCropBlock> TEA = crop("tea", false);
    public static final Supplier<AromaCropBlock> RYE = crop("rye", false);
    public static final Supplier<AromaCropBlock> AGAVE = crop("agave", false);
    public static final Supplier<AromaCropBlock> JUNIPER = crop("juniper", true);
    public static final Supplier<AromaCropBlock> MILLET = crop("millet", false);
    public static final Supplier<AromaCropBlock> ANISE = crop("anise", false);
    public static final Supplier<AromaCropBlock> ALMOND = crop("almond", true);
    public static final Supplier<AromaCropBlock> WALNUT = crop("walnut", true);
    public static final Supplier<AromaCropBlock> COFFEE = crop("coffee", true);
    public static final Supplier<AromaCropBlock> VANILLA = crop("vanilla", false);
    public static final Supplier<AromaCropBlock> CINNAMON = crop("cinnamon", true);
    public static final Supplier<AromaCropBlock> GINGER = crop("ginger", false);
    public static final Supplier<AromaCropBlock> NUTMEG = crop("nutmeg", true);

    public static final Supplier<Block> COOKING_POT = BLOCKS.register("cooking_pot",
            () -> new CookingPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.0F)));

    private ModBlocks() {}
}
