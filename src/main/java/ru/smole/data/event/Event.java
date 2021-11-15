package ru.smole.data.event;

import ru.smole.OpPrison;

import java.util.function.Consumer;

public interface Event {

    String getId();

    <T> Consumer<T> getConsumer();

    String getName();

    String[] getDescription();

    Event start();

    Event start(int time);

    default void stop() {
        getEventManager().stop(-1, getId());
    };

    static EventManager getEventManager() {
        return OpPrison.getInstance().getEventManager();
    }
}
