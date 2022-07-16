package net.core.coremusic.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DirectoryWatcher implements Runnable {

    private static DirectoryWatcher instance;

    private final List<DirectoryWatcherCallBack> callBacks = new ArrayList<>();

    private final WatchService service;

    private final Thread thread = new Thread(this);

    private final Map<WatchKey, Path> keyMap = new HashMap<>();

    private DirectoryWatcher() throws IOException {
        service = FileSystems.getDefault().newWatchService();
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
                var eventDir = keyMap.get(watchKey);

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    for (DirectoryWatcherCallBack callBack : callBacks)
                        callBack.onResult(event, eventDir);
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

    public WatchService getService() {
        return service;
    }

    public void register(@NotNull Path path, WatchEvent.Kind<?>... events) throws IOException {
        keyMap.put(path.register(service, events), path);
    }
}
