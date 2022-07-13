package net.core.coremusic.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class DirectoryWatcher implements Runnable {

    private static DirectoryWatcher instance;

    private final List<DirectoryWatcherCallBack> callBacks = new ArrayList<>();

    private final WatchService service;

    private Thread thread = new Thread(this);

    private DirectoryWatcher() throws IOException {
        service = FileSystems.getDefault().newWatchService();

        var configManager = AppConfigManager.getInstance();
        if (configManager.getMusicDir().isEmpty())
            throw new NoSuchElementException("music dir path cannot be empty");

        configManager.getMusicDir().get().toPath().register(
                service,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );

        thread.start();
    }

    public static DirectoryWatcher getInstance() {
        if (instance == null) {
            try {
                instance = new DirectoryWatcher();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return instance;
    }

    @Override
    public void run() {
        WatchKey watchKey;

        do {
            try {
                watchKey = service.take();
            } catch (InterruptedException e) {
                thread.interrupt();
                break;
            }

            if (!callBacks.isEmpty()) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    for (DirectoryWatcherCallBack callBack : callBacks)
                        callBack.onResult(event);
                }
            }

        }while (!thread.isInterrupted() && watchKey.reset());
    }

    public void interrupt() {
        if (!thread.isInterrupted())
            thread.interrupt();
    }

    public boolean isInterrupted() {
        return thread.isInterrupted();
    }

    public void addCallBack(@NotNull DirectoryWatcherCallBack callBack) {
        callBacks.add(callBack);
    }

    public List<DirectoryWatcherCallBack> getCallBacks() {
        return callBacks;
    }

    public void start() {
        interrupt();

        thread = new Thread(this);
        thread.start();
    }
}
