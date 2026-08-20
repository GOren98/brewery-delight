package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.cooking.CookingPotMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, BreweryDelight.MOD_ID);

    public static final Supplier<MenuType<CookingPotMenu>> COOKING_POT = MENUS.register("cooking_pot",
            () -> new MenuType<>(CookingPotMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}
}
