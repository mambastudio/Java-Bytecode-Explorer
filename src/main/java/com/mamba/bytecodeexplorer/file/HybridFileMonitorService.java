package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class HybridFileMonitorService implements FileMonitorService {
    public enum Backend {
        NATIVE, POLLING
    }

    @FunctionalInterface
    public interface MonitorPolicy {
        Backend backendFor(AbstractFileRefTree<?> tree);
    }

    private final FileRefWatcher nativeMonitor;
    private final PollingFileMonitor pollingMonitor;
    private final MonitorPolicy policy;

    public HybridFileMonitorService() {
        this(HybridFileMonitorService::nativeBackend);
    }

    public HybridFileMonitorService(MonitorPolicy policy) {
        this(new FileRefWatcher(), new PollingFileMonitor(), policy);
    }

    public HybridFileMonitorService(
            FileRefWatcher nativeMonitor,
            PollingFileMonitor pollingMonitor,
            MonitorPolicy policy) {
        this.nativeMonitor = Objects.requireNonNull(nativeMonitor, "nativeMonitor must not be null");
        this.pollingMonitor = Objects.requireNonNull(pollingMonitor, "pollingMonitor must not be null");
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
    }

    public static Backend nativeBackend(AbstractFileRefTree<?> tree) {
        return Backend.NATIVE;
    }

    public static Backend pollingBackend(AbstractFileRefTree<?> tree) {
        return Backend.POLLING;
    }

    @Override
    public MonitorHandle monitorTree(AbstractFileRefTree<?> tree, FileEventListener listener) {
        return switch(policy.backendFor(tree)) {
            case NATIVE -> nativeMonitor.monitorTree(tree, listener);
            case POLLING -> pollingMonitor.monitorTree(tree, listener);
        };
    }

    @Override
    public int monitoredCount() {
        return nativeMonitor.monitoredCount() + pollingMonitor.monitoredCount();
    }

    @Override
    public Map<Path, FileRefMeta> metaMap() {
        return nativeMonitor.metaMap();
    }

    @Override
    public void close() throws IOException {
        IOException failure = null;
        try {
            nativeMonitor.close();
        } catch (IOException ex) {
            failure = ex;
        }
        try {
            pollingMonitor.close();
        } catch (IOException ex) {
            if(failure == null)
                failure = ex;
            else
                failure.addSuppressed(ex);
        }
        if(failure != null)
            throw failure;
    }
}
