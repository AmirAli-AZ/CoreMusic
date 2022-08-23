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
        path = Paths.get(System.getProperty("user.home") + File.separator + ".net.core" + File.separator + "coremusic");

        if (OS.isWindows())
            path = Paths.get(System.getenv("APPDATA") + File.separator + ".net.core"  + File.separator + "coremusic");
    }

    public static Path getAppData() {
        try {
            var dir = Files.createDirectories(path);
            if (OS.isWindows())
                hide(dir.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    private static void hide(@NotNull Path path) throws IOException {
        var dosFileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
        var dosFileAttributes = dosFileAttributeView.readAttributes();

        if (!dosFileAttributes.isHidden())
            dosFileAttributeView.setHidden(true);
    }
}
