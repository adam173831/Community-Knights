package com.example.app.taskmanagement.ui.util;

import com.vaadin.flow.component.UI;
import java.util.Map;

public final class ThemeUtil {

    private static final String DEFAULT_PRIMARY_COLOR = "#1a73e8";
    private static final String DEFAULT_TEXT_COLOR = "#ffffff";
    private static final ThemePalette DEFAULT_PALETTE =
        new ThemePalette(DEFAULT_PRIMARY_COLOR, DEFAULT_TEXT_COLOR);

    private static final Map<String, ThemePalette> COLOR_SCHEMES = Map.ofEntries(
        Map.entry("Blue (Default)", DEFAULT_PALETTE),
        Map.entry("Green", new ThemePalette("#0f9960", "#ffffff")),
        Map.entry("Purple", new ThemePalette("#7b3fe4", "#ffffff")),
        Map.entry("Red", new ThemePalette("#d93025", "#ffffff")),
        Map.entry("Orange", new ThemePalette("#f57c00", "#ffffff"))
    );

    // nobody should instantiate this helper
    private ThemeUtil() {}

    // Light / Dark / System Default
    public static void applyTheme(UI ui, String theme) {
        if (ui == null) {
            return;
        }

        if (theme == null || theme.isBlank()) {
            // "System Default": no explicit theme attribute
            ui.getElement().removeAttribute("theme");
            return;
        }

        if ("Dark".equalsIgnoreCase(theme)) {
            ui.getElement().setAttribute("theme", "dark");
        } else {
            // Light or anything else clears the dark attribute
            ui.getElement().removeAttribute("theme");
        }
    }

    // Accent color palette
    public static void applyColorScheme(UI ui, String colorScheme) {
        if (ui == null) {
            return;
        }

        ThemePalette palette = COLOR_SCHEMES.getOrDefault(colorScheme, DEFAULT_PALETTE);

        ui.getPage().executeJs(
            "const root = document.documentElement;" +
                "root.style.setProperty('--lumo-primary-color', $0);" +
                "root.style.setProperty('--lumo-primary-color-10pct', $1);" +
                "root.style.setProperty('--lumo-primary-color-50pct', $2);" +
                "root.style.setProperty('--lumo-primary-text-color', $3);" +
                "root.style.setProperty('--lumo-primary-contrast-color', $3);" +
                "root.style.setProperty('--lumo-primary-color-rgb', $4);",
            palette.primaryColor(),
            palette.primaryColor10(),
            palette.primaryColor50(),
            palette.primaryTextColor(),
            palette.primaryColorRgb()
        );
    }

    // Small immutable color bundle
    private record ThemePalette(String primaryColor, String primaryTextColor) {
        private ThemePalette {
            primaryColor = normalizeHex(primaryColor);
            primaryTextColor = normalizeHex(primaryTextColor);
        }

        String primaryColor10() {
            return toRgba(primaryColor, 0.1);
        }

        String primaryColor50() {
            return toRgba(primaryColor, 0.5);
        }

        String primaryColorRgb() {
            int red = parseComponent(primaryColor, 1);
            int green = parseComponent(primaryColor, 3);
            int blue = parseComponent(primaryColor, 5);
            return red + "," + green + "," + blue;
        }
    }

    // ---- helpers for ThemePalette / style ----

    private static String normalizeHex(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_PRIMARY_COLOR;
        }

        String trimmed = value.trim();
        if (!trimmed.startsWith("#")) {
            trimmed = "#" + trimmed;
        }

        // #RRGGBB
        if (trimmed.length() == 7) {
            return trimmed.toLowerCase();
        }

        // #RGB -> expand to #RRGGBB
        if (trimmed.length() == 4) {
            char r = trimmed.charAt(1);
            char g = trimmed.charAt(2);
            char b = trimmed.charAt(3);
            return ("#" + r + r + g + g + b + b).toLowerCase();
        }

        // fallback
        return DEFAULT_PRIMARY_COLOR;
    }

    private static String toRgba(String hex, double alpha) {
        int red = parseComponent(hex, 1);
        int green = parseComponent(hex, 3);
        int blue = parseComponent(hex, 5);
        return String.format("rgba(%d,%d,%d,%.2f)", red, green, blue, alpha);
    }

    private static int parseComponent(String hex, int startIndex) {
        return Integer.parseInt(hex.substring(startIndex, startIndex + 2), 16);
    }
}
