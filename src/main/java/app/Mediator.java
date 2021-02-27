package app;

import java.util.concurrent.BlockingDeque;

public interface Mediator {
    void notifyEOF(BlockingDeque<String> queue);
    void subscribe(Subscriber subscriber);
}
