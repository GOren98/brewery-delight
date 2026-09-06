package dev.goren98.brewerydelight.aroma;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.goren98.brewerydelight.BreweryDelight;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable-at-runtime Aroma metadata registry. Definitions are read from the bundled JSON once
 * during mod construction; Minecraft /reload intentionally does not replace this registry.
 */
public final class AromaDefinitions {
    public static final String RESOURCE_PATH = "/assets/brewerydelight/aroma/definitions.json";
    private static final Map<String, AromaDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static boolean loaded;

    static {
        register(new AromaDefinition("apple", "Apple", AromaType.INGREDIENT, AromaRank.NORMAL, "apple", "", ""));
        register(new AromaDefinition("kiwi", "Kiwi", AromaType.INGREDIENT, AromaRank.NORMAL, "kiwi", "", ""));
    }

    public static synchronized void loadBundled() {
        if (loaded) return;
        loaded = true;
        try (InputStream stream = AromaDefinitions.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                BreweryDelight.LOGGER.info("No bundled Aroma definitions found at {}; using base-item fallbacks", RESOURCE_PATH);
                return;
            }
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            JsonArray entries = parsed.isJsonArray() ? parsed.getAsJsonArray()
                    : requiredArray(parsed.getAsJsonObject(), "aromas");
            Map<String, AromaDefinition> next = new LinkedHashMap<>();
            for (JsonElement element : entries) {
                AromaDefinition definition = parse(element.getAsJsonObject());
                if (next.putIfAbsent(definition.id(), definition) != null) {
                    throw new IllegalStateException("Duplicate Aroma id: " + definition.id());
                }
            }
            if (!next.isEmpty()) {
                validateParents(next);
                DEFINITIONS.clear();
                DEFINITIONS.putAll(next);
            }
            BreweryDelight.LOGGER.info("Loaded {} bundled Aroma definitions", next.size());
        } catch (RuntimeException | java.io.IOException exception) {
            throw new IllegalStateException("Invalid bundled Aroma definitions at " + RESOURCE_PATH, exception);
        }
    }

    private static AromaDefinition parse(JsonObject json) {
        String id = requiredString(json, "id");
        String displayName = optionalString(json, "display_name", id);
        AromaType type = parseEnum(AromaType.class, requiredString(json, "type"));
        AromaRank rank = parseEnum(AromaRank.class, requiredString(json, "rank"));
        return new AromaDefinition(id, displayName, type, rank,
                optionalString(json, "ingredient_family", ""),
                optionalString(json, "line", ""), optionalString(json, "parent", ""));
    }

    private static void validateParents(Map<String, AromaDefinition> definitions) {
        for (AromaDefinition definition : definitions.values()) {
            if (!definition.parent().isBlank() && !definitions.containsKey(definition.parent())) {
                throw new IllegalStateException("Aroma " + definition.id() + " has unknown parent " + definition.parent());
            }
            Set<String> visited = new HashSet<>();
            String current = definition.id();
            while (!current.isBlank()) {
                if (!visited.add(current)) {
                    throw new IllegalStateException("Cyclic Aroma parent chain containing " + current);
                }
                AromaDefinition currentDefinition = definitions.get(current);
                current = currentDefinition == null ? "" : currentDefinition.parent();
            }
        }
    }

    private static JsonArray requiredArray(JsonObject json, String name) {
        if (!json.has(name) || !json.get(name).isJsonArray()) throw new IllegalStateException("Missing array: " + name);
        return json.getAsJsonArray(name);
    }

    private static String requiredString(JsonObject json, String name) {
        String value = optionalString(json, name, "");
        if (value.isBlank()) throw new IllegalStateException("Missing string: " + name);
        return value;
    }

    private static String optionalString(JsonObject json, String name, String fallback) {
        return json.has(name) && json.get(name).isJsonPrimitive() ? json.get(name).getAsString() : fallback;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        String normalized = value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        if (type == AromaType.class && normalized.equals("SENSORY")) normalized = "CHARACTER";
        return Enum.valueOf(type, normalized);
    }

    public static synchronized void register(AromaDefinition definition) {
        AromaDefinition previous = DEFINITIONS.putIfAbsent(definition.id(), definition);
        if (previous != null) throw new IllegalArgumentException("Duplicate Aroma id: " + definition.id());
    }

    public static Optional<AromaDefinition> find(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        AromaDefinition known = DEFINITIONS.get(id);
        if (known != null) return Optional.of(known);
        // Base crop Aromas remain usable before the generated JSON is added.
        return Optional.of(new AromaDefinition(id, id, AromaType.INGREDIENT, AromaRank.NORMAL, id, "", ""));
    }

    public static String ingredientFamily(String aromaId) {
        AromaDefinition definition = aromaId == null ? null : DEFINITIONS.get(aromaId);
        return definition == null ? "" : definition.ingredientFamily();
    }

    public static String line(String aromaId) {
        AromaDefinition definition = aromaId == null ? null : DEFINITIONS.get(aromaId);
        return definition == null ? "" : definition.line();
    }

    /** Returns true when aromaId is the requested ancestor or one of its descendants. */
    public static boolean isSelfOrDescendantOf(String aromaId, String ancestorId) {
        if (aromaId == null || aromaId.isBlank() || ancestorId == null || ancestorId.isBlank()) return false;
        Set<String> visited = new HashSet<>();
        String current = aromaId;
        while (!current.isBlank() && visited.add(current)) {
            if (ancestorId.equals(current)) return true;
            AromaDefinition definition = DEFINITIONS.get(current);
            current = definition == null ? "" : definition.parent();
        }
        return false;
    }

    private AromaDefinitions() {}
}
