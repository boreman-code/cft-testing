package app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * Notifies subscribers about the end of a file
 */
public class MediatorImpl implements Mediator {

    List<Subscriber> subscribers = new ArrayList<>();

    @Override
    public void notifyEOF(BlockingDeque<String> queue) {
        for (Subscriber subscriber : subscribers) {
            subscriber.respondToNotification(queue);
        }
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }
}
