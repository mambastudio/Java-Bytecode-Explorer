package com.mamba.bytecodeexplorer.file;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class FileWatcherRegistry {

    private static final Map<File, FileWatcher> watchers = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService healthChecker =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FileWatcher-HealthChecker");
            t.setDaemon(true);
            return t;
        });

    static {
        // Check every 5 seconds
        healthChecker.scheduleAtFixedRate(FileWatcherRegistry::checkRoots, 2, 2, TimeUnit.SECONDS);
    }
    
    private static void checkRoots() {
        for (File root : watchers.keySet()) {
            System.out.println("Health check on: " + root);
            if (root.isDirectory() && !root.exists()) {
        //        watchers.get(root).fireRootRemoved();
          //      watchers.remove(root);
            }
        }
    }

    private FileWatcherRegistry() {} // utility class, no instantiation

    /** Get existing watcher for a path, or create/start a new one.
     * @param root
     * @param mode
     * @return 
     * @throws java.io.IOException */
    public static FileWatcher getOrCreate(File root, FileWatcher.Mode mode){
        File abs = root.getAbsoluteFile();
        if (!abs.isDirectory()) {
            throw new IllegalArgumentException("FileWatcher requires a directory: " + abs);
        }
        return watchers.computeIfAbsent(abs, f -> {
            try {
                FileWatcher w = new FileWatcher(f, mode);
                w.start();
                return w;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Stop and remove watcher for a given path.
     * @param root */
    public static void stop(File root) {
        FileWatcher w = watchers.remove(root.getAbsoluteFile());
        if (w != null) {
            w.stop();
        }
    }

    /** Stop and remove all watchers. */
    public static void stopAll() {
        for (FileWatcher w : watchers.values()) {
            w.stop();
        }
        watchers.clear();
    }
}
