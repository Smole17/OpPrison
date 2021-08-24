package ru.smole.data.event;

import org.bukkit.event.block.BlockBreakEvent;
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

    public static void asyncChat(AsyncPlayerChatEvent event) {
        if (!chatEvents.isEmpty()) {
            chatEvents.forEach((s, asyncPlayerChatEventConsumer) -> {
                if (activeEvents.contains(s)) {
                    asyncPlayerChatEventConsumer.accept(event);
                }
            });
        }
    }

    public static void blockBreak(BlockBreakEvent event) {
        if (!breakEvents.isEmpty()) {
            breakEvents.forEach((s, blockBreakEventConsumer) -> {
                if (activeEvents.contains(s)) {
                    blockBreakEventConsumer.accept(event);
                }
            });
        }
    }
}
