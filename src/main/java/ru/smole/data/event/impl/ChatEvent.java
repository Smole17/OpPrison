package ru.smole.data.event.impl;

import lombok.Data;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;
import ru.smole.data.event.Event;
import ru.xfenilafs.core.util.Schedules;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class ChatEvent implements Event {

    private String id;
    private String[] description;
    private String name;
    private Function<ChatEvent, Runnable> runnable;
    private Consumer<AsyncPlayerChatEvent> consumer;

    private BukkitTask task;

    private ChatEvent(String id, String[] description, String name, Consumer<AsyncPlayerChatEvent> consumer) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.consumer = consumer;
    }

    public static ChatEvent get() {
        return new ChatEvent(null, null, null, null);
    }

    public ChatEvent id(String id) {
        setId(id);
        return this;
    }

    public ChatEvent description(String... desc) {
        setDescription(desc);
        return this;
    }

    public ChatEvent name(String name) {
        setName(name);
        return this;
    }

    public ChatEvent runnable(Function<ChatEvent, Runnable> runnable) {
        setRunnable(runnable);
        return this;
    }

    public ChatEvent consumer(Consumer<AsyncPlayerChatEvent> consumer) {
        setConsumer(consumer);
        return this;
    }

    public ChatEvent start() {
        if (id == null)
            throw new NullPointerException(this.getClass().getName() + ": id is null");

        if (name == null)
            throw new NullPointerException(this.getClass().getName() + ": name is null");

        Event.getEventManager().start(0, this);
        return this;
    }

    public ChatEvent start(int time) {
        if (id == null)
            throw new NullPointerException(this.getClass().getName() + ": id is null");

        if (name == null)
            throw new NullPointerException(this.getClass().getName() + ": name is null");

        Event.getEventManager().start(0, this);

        if (runnable != null)
            task = runnable.andThen(runnable1 -> task = Schedules.runAsync(runnable1, time)).apply(this);

        return this;
    }

    public void stop() {
        Event.getEventManager().stop(0, getId());

        if (task != null && !task.isCancelled())
            task.cancel();
    }
}
