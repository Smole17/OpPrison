package ru.smole.event;

import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface OpEvent {

    List<String> activeEvents = new ArrayList<>();
    Map<String, Consumer<AsyncPlayerChatEvent>> events = new HashMap<>();

    void start(String name);

    void stop(String name);

    default Map<String, Consumer<AsyncPlayerChatEvent>> getEvents() {
        return events;
    }

    default List<String> getActiveEvents() {
        return activeEvents;
    }
}
