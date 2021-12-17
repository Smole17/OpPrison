package ru.smole.data.event;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.smole.data.event.impl.BlockEvent;
import ru.smole.data.event.impl.ChatEvent;
import ru.smole.data.event.impl.PointEvent;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;

public class EventManager {

    private final @Getter Map<String, Event> otherEvents;
    private final @Getter Map<String, ChatEvent> chatEvents;
    private final @Getter Map<String, BlockEvent> blockEvents;

    private final @Getter Map<String, Double> blocks;
    private final @Getter Map<String, List<Location>> treasureMap;

    public EventManager() {
        this.otherEvents = new HashMap<>();
        this.chatEvents = new HashMap<>();
        this.blockEvents = new HashMap<>();

        blocks = new HashMap<>();
        treasureMap = new HashMap<>();
    }

    public void start(int i, Event event) {
        switch (i) {
            case 0:
                if (event != null)
                    chatEvents.put(event.getId(), (ChatEvent) event);
                break;

            case 1:
                if (event != null)
                    blockEvents.put(event.getId(), (BlockEvent) event);
                break;

            default:
                otherEvents.put(event.getId(), event);
                break;
        }

        if (event == null)
            return;

        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Событие &b" + event.getName() + " &fначалось");
        ChatUtil.broadcast("");

        String[] desc = event.getDescription();

        if (desc == null)
            return;

        Arrays.stream(desc).forEach(ChatUtil::broadcast);
        ChatUtil.broadcast("");
    }

    public void stop(int i, String id) {
        switch (i) {
            case 0:
                chatEvents.remove(id);
                break;

            case 1:
                blockEvents.remove(id);
                break;

            default:
                otherEvents.remove(id);
                break;
        }
    }

    public void stop(int i, Event event) {
        stop(i, event.getId());

        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Событие &b" + event.getName() + " &fзавершилось");
        ChatUtil.broadcast("");
    }

    public PointEvent getPointEvent(Collection<Region> regionList) {
        return new PointEvent(regionList);
    }

    public void asyncChat(AsyncPlayerChatEvent event) {
        if (!chatEvents.isEmpty())
            chatEvents.forEach((s, s1) -> {
                if (chatEvents.containsKey(s) && s1.getConsumer() != null)
                    s1.getConsumer().accept(event);
            });
    }

    public void blockBreak(BlockBreakEvent event) {
        if (!blockEvents.isEmpty())
            blockEvents.forEach((s, s1) -> {
                if (blockEvents.containsKey(s) && s1.getConsumer() != null)
                    s1.getConsumer().accept(event);
            });
    }
}
