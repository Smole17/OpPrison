package ru.smole.data.event;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface OpEvent {

    List<String> activeEvents = new ArrayList<>();
    Map<String, Consumer<AsyncPlayerChatEvent>> chatEvents = new HashMap<>();
    Map<String, Consumer<BlockBreakEvent>> breakEvents = new HashMap<>();

    void start(String name);

    void stop(String name);

    default Map<String, Consumer<AsyncPlayerChatEvent>> getChatEvents() {
        return chatEvents;
    }

    default Map<String, Consumer<BlockBreakEvent>> getBreakEvents() {
        return breakEvents;
    }

    default List<String> getActiveEvents() {
        return activeEvents;
    }
}
