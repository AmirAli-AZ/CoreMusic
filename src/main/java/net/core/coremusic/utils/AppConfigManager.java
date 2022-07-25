package net.core.coremusic.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public final class AppConfigManager {

    private static AppConfigManager instance;

    private final Path configPath = Paths.get(Environment.getAppDataPath() + File.separator + "config.properties");

    private final Properties properties = new Properties();

    private AppConfigManager() {
        load();
    }

    public static AppConfigManager getInstance() {
        if (instance == null)
            instance = new AppConfigManager();
        return instance;
    }

    public void load() {
        if (!Files.exists(configPath))
            return;
        try {
            var inputStream = new FileInputStream(configPath.toFile());
            properties.load(inputStream);
            inputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
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

    public void applyThemeToAllWindows(@NotNull Themes theme) {
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage stage) {
                var scene = stage.getScene();

                if (scene.getStylesheets().isEmpty())
                    scene.getStylesheets().add(theme.getPath());
                else
                    scene.getStylesheets().set(0, theme.getPath());
            }
        }

        try {
            saveTheme(theme);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Themes loadTheme() {
        return Themes.findByName(properties.getProperty("theme"));
    }

    private void saveTheme(@NotNull Themes theme) throws IOException {
        properties.setProperty("theme", theme.getName());
        var outputStream = new FileOutputStream(configPath.toFile());
        properties.store(outputStream, "DO NOT EDIT");
        outputStream.close();
    }

    public void setMusicDir(@NotNull File file) throws IOException {
        properties.setProperty("music_dir", file.getAbsolutePath());
        var outputStream = new FileOutputStream(configPath.toFile());
        properties.store(outputStream, "DO NOT EDIT");
        outputStream.close();
    }

    public void setMusicDir(@NotNull Path path) throws IOException {
        setMusicDir(path.toFile());
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

    public Path getConfigPath() {
        return configPath;
    }
}
