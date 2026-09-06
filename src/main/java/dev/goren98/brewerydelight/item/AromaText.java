package dev.goren98.brewerydelight.item;

import dev.goren98.brewerydelight.aroma.AromaDefinitions;

public final class AromaText {
    public static String pretty(String value) {
        StringBuilder out = new StringBuilder();
        for (String part : value.split("_")) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    public static String roman(int value) {
        if (value <= 0) return "0";
        String[] numerals = {"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
                "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
        return value < numerals.length ? numerals[value] : Integer.toString(value);
    }

    public static String displayName(String aromaId) {
        return AromaDefinitions.find(aromaId).map(definition -> definition.displayName()).orElseGet(() -> pretty(aromaId));
    }

    public static String ingredientFamily(String aromaId) {
        String family = AromaDefinitions.ingredientFamily(aromaId);
        return family.isBlank() ? "" : pretty(family);
    }

    private AromaText() {}
}
