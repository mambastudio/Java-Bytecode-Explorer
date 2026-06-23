package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface FileMonitorService extends AutoCloseable {

    interface FileRefMeta {}

    MonitorHandle monitorTree(AbstractFileRefTree<?> tree, FileEventListener listener);

    int monitoredCount();

    Map<Path, FileRefMeta> metaMap();

    @Override
    void close() throws IOException;

    interface MonitorHandle extends AutoCloseable {
        void stop();

        @Override
        default void close() {
            stop();
        }
    }
}
