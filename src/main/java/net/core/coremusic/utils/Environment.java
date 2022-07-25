package net.core.coremusic.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;

public final class Environment {

    private static Path path;

    static {
        var path = System.getProperty("user.home") + File.separator + ".net.core.coremusic";

        if (OS.isWindows())
            path = System.getenv("APPDATA") + File.separator + ".net.core.coremusic";
        Environment.path = Paths.get(path);
    }

    public static File getAppData() {
        try {
            var dir = Files.createDirectories(path);
            hide(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path.toFile();
    }

    public static Path getAppDataPath() {
        return getAppData().toPath();
    }

    private static void hide(@NotNull Path path) throws IOException {
        var dosFileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
        var dosFileAttributes = dosFileAttributeView.readAttributes();

        if (!dosFileAttributes.isHidden())
            dosFileAttributeView.setHidden(true);
    }
}
