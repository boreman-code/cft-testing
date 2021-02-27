package app;

import java.util.concurrent.BlockingDeque;

public interface Subscriber {
    void respondToNotification(BlockingDeque<String> queue);
}
