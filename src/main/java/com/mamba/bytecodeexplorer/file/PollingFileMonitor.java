package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener;
import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener.FileEvent;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class PollingFileMonitor implements FileMonitorService {
    public static final long DEFAULT_INTERVAL_MILLIS = 1_000;

    private final ScheduledExecutorService executor;
    private final long intervalMillis;
    private final AtomicInteger count = new AtomicInteger();
    private final Map<Path, FileRefMeta> metaMap = new ConcurrentHashMap<>();

    public PollingFileMonitor() {
        this(DEFAULT_INTERVAL_MILLIS);
    }

    public PollingFileMonitor(long intervalMillis) {
        if(intervalMillis <= 0)
            throw new IllegalArgumentException("intervalMillis must be positive");

        this.intervalMillis = intervalMillis;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PollingFileMonitor-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public MonitorHandle monitorTree(AbstractFileRefTree<?> tree, FileEventListener listener) {
        Objects.requireNonNull(tree, "tree must not be null");
        Objects.requireNonNull(listener, "listener must not be null");

        List<Path> roots = pollingRoots(tree);
        if(roots.isEmpty())
            return () -> {};

        PollingHandle handle = new PollingHandle(roots, listener);
        handle.start();
        count.incrementAndGet();
        return handle;
    }

    @Override
    public int monitoredCount() {
        return count.get();
    }

    @Override
    public Map<Path, FileRefMeta> metaMap() {
        return metaMap;
    }

    @Override
    public void close() throws IOException {
        executor.shutdownNow();
    }

    private List<Path> pollingRoots(AbstractFileRefTree<?> tree) {
        var roots = new ArrayList<Path>();
        collectPollingRoots(tree, roots);
        return roots;
    }

    private void collectPollingRoots(AbstractFileRefTree<?> tree, List<Path> roots) {
        if(tree.ref() instanceof RealFile file && file.isDirectory()) {
            roots.add(file.path());
            return;
        }

        for(var child : tree.children())
            if(child instanceof AbstractFileRefTree<?> childTree)
                collectPollingRoots(childTree, roots);
    }

    private Map<Path, Long> snapshot(List<Path> roots) {
        var snapshot = new HashMap<Path, Long>();
        for(Path root : roots)
            snapshot.putAll(snapshot(root));
        return snapshot;
    }

    private Map<Path, Long> snapshot(Path root) {
        var snapshot = new HashMap<Path, Long>();
        if(!Files.isDirectory(root))
            return snapshot;

        try(Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".class"))
                    .forEach(path -> {
                        try {
                            snapshot.put(path, Files.getLastModifiedTime(path).toMillis());
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }

        return snapshot;
    }

    private final class PollingHandle implements MonitorHandle {
        private final List<Path> roots;
        private final FileEventListener listener;
        private volatile Map<Path, Long> previous;
        private ScheduledFuture<?> future;
        private volatile boolean stopped;

        PollingHandle(List<Path> roots, FileEventListener listener) {
            this.roots = List.copyOf(roots);
            this.listener = listener;
            this.previous = snapshot(this.roots);
        }

        void start() {
            future = executor.scheduleWithFixedDelay(this::poll,
                    intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
        }

        private void poll() {
            if(stopped)
                return;

            Map<Path, Long> current = snapshot(roots);
            emitChanges(previous, current);
            previous = current;
        }

        private void emitChanges(Map<Path, Long> oldSnapshot, Map<Path, Long> newSnapshot) {
            Set<Path> all = new HashSet<>();
            all.addAll(oldSnapshot.keySet());
            all.addAll(newSnapshot.keySet());

            for(Path file : all) {
                Long oldModified = oldSnapshot.get(file);
                Long newModified = newSnapshot.get(file);
                Path parent = file.getParent();

                if(oldModified == null && newModified != null) {
                    listener.onEvent(new FileEvent.Created(parent, file));
                } else if(oldModified != null && newModified == null) {
                    listener.onEvent(new FileEvent.Deleted(parent, file));
                } else if(!Objects.equals(oldModified, newModified)) {
                    listener.onEvent(new FileEvent.Modified(parent, file));
                }
            }
        }

        @Override
        public void stop() {
            if(stopped)
                return;

            stopped = true;
            if(future != null)
                future.cancel(false);
            count.decrementAndGet();
        }
    }
}
