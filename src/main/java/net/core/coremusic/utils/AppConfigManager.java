package net.core.coremusic.utils;

import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public final class AppConfigManager {

    private static AppConfigManager instance;

    private final File configFile = new File(Environment.getAppDataPath() + File.separator + "config.properties");

    private final Properties properties = new Properties();

    private final List<ThemeChangedListener> listeners = new ArrayList<>();

    private AppConfigManager() {
        refresh();
    }

    public static AppConfigManager getInstance() {
        if (instance == null)
            instance = new AppConfigManager();
        return instance;
    }

    public void refresh() {
        if (configFile.exists()) {
            try {
                var inputStream = new FileInputStream(configFile);
                properties.load(inputStream);
                inputStream.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTheme(@NotNull Themes theme, @NotNull Scene scene) {
        if (scene.getStylesheets().isEmpty())
            scene.getStylesheets().add(theme.getPath());
        else
            scene.getStylesheets().set(0, theme.getPath());

        try {
            saveTheme(theme);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setThemeAndCallListeners(@NotNull Themes theme, @NotNull Scene scene) {
        setTheme(theme, scene);
        for (ThemeChangedListener listener : listeners)
            listener.onChanged(theme);
    }

    public Themes loadTheme() {
        return Themes.findByName(properties.getProperty("theme"));
    }

    private void saveTheme(@NotNull Themes theme) throws IOException {
        properties.setProperty("theme", theme.getName());
        var outputStream = new FileOutputStream(configFile);
        properties.store(outputStream, "DO NOT EDIT");
        outputStream.close();
    }

    public void setMusicDir(@NotNull File file) throws IOException {
        properties.setProperty("music_dir", file.getAbsolutePath());
        var outputStream = new FileOutputStream(configFile);
        properties.store(outputStream, "DO NOT EDIT");
        outputStream.close();
    }

    public void setMusicDir(@NotNull String path) throws IOException {
        setMusicDir(new File(path));
    }

    public Optional<File> getMusicDir() {
        var musicDir = properties.getProperty("music_dir");
        if (musicDir != null)
            return Optional.of(new File(musicDir));
        return Optional.empty();
    }

    public Optional<Path> getMusicDirPath() {
        return getMusicDir().map(File::toPath);
    }

    public void addThemeChangedListener(@NotNull ThemeChangedListener listener) {
        listeners.add(listener);
    }

    public List<ThemeChangedListener> getThemeChangedListeners() {
        return listeners;
    }
}
