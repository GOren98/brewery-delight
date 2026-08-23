package dev.goren98.brewerydelight.aroma;

import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Shared material-count calculation used by Base and Liqueur production. */
public final class PrimaryAromaCalculator {
    public static Optional<Result> calculate(Iterable<ItemStack> materials) {
        Map<String, Integer> counts = new HashMap<>();
        for (ItemStack material : materials) {
            AromaItems.currentAromaId(material).ifPresent(aroma -> counts.merge(aroma, 1, Integer::sum));
        }
        return counts.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new Result(entry.getKey(), entry.getValue()));
    }

    public record Result(String aromaId, int materialCount) {}
    private PrimaryAromaCalculator() {}
}
