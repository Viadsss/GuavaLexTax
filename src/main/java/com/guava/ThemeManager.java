package com.guava;

import java.util.*;
import java.awt.*;

public class ThemeManager {
    private static final Map<String, Map<String, Color>> themes = Map.of(
        "light", Map.ofEntries(
            // Catppuccin Latte Theme
            Map.entry("default", new Color(0, 0, 0)), // Text
            Map.entry("keyword", new Color(210, 15, 57)), // Red
            Map.entry("type", new Color(136, 57, 239)), // Mauve
            Map.entry("component", new Color(254, 100, 11)), // Peach
            Map.entry("string", new Color(64, 160, 43)), // Green
            Map.entry("number", new Color(223, 142, 29)), // Yellow
            Map.entry("operator", new Color(234, 118, 203)), // Pink
            Map.entry("comment", new Color(108, 111, 133)), // Subtext 0
            Map.entry("boolean", new Color(230, 69, 83)), // Maroon
            Map.entry("null", new Color(221, 120, 120)), // Flamingo
            Map.entry("char", new Color(64, 160, 43)) // Green (Same as strings)
        ),
        "dark", Map.ofEntries(
            // Catppuccin Mocha Theme
            Map.entry("default", new Color(255, 255, 255)), // Text
            Map.entry("keyword", new Color(243, 139, 168)), // Red
            Map.entry("type", new Color(203, 166, 247)), // Mauve
            Map.entry("component", new Color(250, 179, 135)), // Peach
            Map.entry("string", new Color(166, 227, 161)), // Green
            Map.entry("number", new Color(249, 226, 175)), // Yellow
            Map.entry("operator", new Color(245, 194, 231)), // Pink
            Map.entry("comment", new Color(108, 112, 134)), // Overlay 0
            Map.entry("boolean", new Color(235, 160, 172)), // Maroon
            Map.entry("null", new Color(242, 205, 205)), // Flamingo
            Map.entry("char", new Color(166, 227, 161)) // Green (Same as strings)
        )
    );

    private static Map<String, Color> currentTheme = themes.get("light");

    public static void setTheme(String themeName) {
        currentTheme = themes.getOrDefault(themeName.toLowerCase(), themes.get("light"));
    }

    public static Map<String, Color> getCurrentTheme() {
        return currentTheme;
    }    
}
