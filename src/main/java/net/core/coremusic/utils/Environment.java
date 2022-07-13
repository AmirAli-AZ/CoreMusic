package net.core.coremusic.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class Environment {

    public static File getAppData() {
        var path = System.getProperty("user.home") + File.separator + "net.core.coremusic";

        if (OS.isWindows())
            path = System.getenv("APPDATA") + File.separator + "net.core.coremusic";
        var file = new File(path);
        try {
            Files.createDirectories(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static String getAppDataPath() {
        return getAppData().getAbsolutePath();
    }
}
