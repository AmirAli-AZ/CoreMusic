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

    public static Path getAppData() {
        try {
            var dir = Files.createDirectories(path);
            if (OS.isWindows())
                setHideAttribute(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    private static void setHideAttribute(@NotNull Path path) throws IOException {
        var dosFileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
        var dosFileAttributes = dosFileAttributeView.readAttributes();

        if (!dosFileAttributes.isHidden())
            dosFileAttributeView.setHidden(true);
    }
}
