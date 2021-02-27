package app;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public interface Sorter extends Closeable {
    void sort(List<BlockingDeque<String>> queues);
}
