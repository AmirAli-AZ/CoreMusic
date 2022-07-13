package net.core.coremusic.utils;

import org.jetbrains.annotations.NotNull;

import java.nio.file.WatchEvent;

public interface DirectoryWatcherCallBack {
    void onResult(@NotNull WatchEvent<?> event);
}
