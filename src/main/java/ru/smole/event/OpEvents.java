package ru.smole.event;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class OpEvents implements OpEvent {

    @Override
    public void start(String name) {
        activeEvents.add(name);
    }

    @Override
    public void stop(String name) {
        activeEvents.remove(name);
    }

    public void asyncChat(AsyncPlayerChatEvent event) {
        if (!events.isEmpty()) {
            events.forEach((s, asyncPlayerChatEventConsumer) -> {
                if (activeEvents.contains(s)) {
                    asyncPlayerChatEventConsumer.accept(event);
                }
            });
        }
    }
}
