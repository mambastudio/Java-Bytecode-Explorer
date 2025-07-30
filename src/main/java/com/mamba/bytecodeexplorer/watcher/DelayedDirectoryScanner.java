package com.mamba.bytecodeexplorer.watcher;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;
import java.util.logging.*;

public class DelayedDirectoryScanner {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true); // This makes the thread non-blocking for JVM shutdown
                t.setName("DelayedDirectoryScanner-Thread");
                return t;
            });
    private final FileRefWatcherListener listener;

    public DelayedDirectoryScanner(FileRefWatcherListener listener) {
        this.listener = listener;
    }

    public void scanLater(FileRef root, long delayMillis) {
        if(!root.isDirectory())
            return;
        scheduler.schedule(() -> {
            try {
                Files.walkFileTree(root.path(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if (!dir.equals(root.path())) {
                            FileRef ref = new FileRef(dir);
                            listener.onCreate(new FileRef(dir.getParent()), ref);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        FileRef ref = new FileRef(file);
                        listener.onCreate(new FileRef(file.getParent()), ref);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Logger.getLogger(DelayedDirectoryScanner.class.getName())
                      .log(Level.WARNING, "Delayed directory scan failed", e);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
