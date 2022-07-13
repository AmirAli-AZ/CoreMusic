package net.core.coremusic.utils;

import java.util.Objects;

public enum Themes {
    LIGHT("light", Objects.requireNonNull(Themes.class.getResource("/net/core/coremusic/themes/light-theme.css")).toExternalForm()),
    DARK("dark", Objects.requireNonNull(Themes.class.getResource("/net/core/coremusic/themes/dark-theme.css")).toExternalForm());

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
        else if (name.equalsIgnoreCase("dark"))
            return DARK;
        else
            return LIGHT;
    }
}
