package ru.smole.data.event.data;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.scheduler.BukkitTask;
import ru.smole.OpPrison;
import ru.smole.data.event.EventManager;

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
