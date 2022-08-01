package net.core.coremusic.utils;

import java.util.Objects;

public enum Themes {
    LIGHT("light", Objects.requireNonNull(Themes.class.getResource("/net/core/coremusic/themes/light-theme.css")).toExternalForm()),
    DARK("dark", Objects.requireNonNull(Themes.class.getResource("/net/core/coremusic/themes/dark-theme.css")).toExternalForm()),
    MANJARO_DARK("manjaro-dark", Objects.requireNonNull(Themes.class.getResource("/net/core/coremusic/themes/manjaro-dark-theme.css")).toExternalForm()),
    MANJARO_LIGHT("manjaro-light", Objects.requireNonNull(Themes.class.getResource("/net/core/coremusic/themes/manjaro-light-theme.css")).toExternalForm());

    private final String name, path;

    Themes(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public static Themes findByName(String name) {
        if (name == null)
            return LIGHT;

        return switch (name.toLowerCase()) {
            case "dark" -> DARK;
            case "manjaro-dark" -> MANJARO_DARK;
            case "manjaro-light" -> MANJARO_LIGHT;

            default -> LIGHT;
        };
    }
}
