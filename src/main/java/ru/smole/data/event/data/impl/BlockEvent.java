package ru.smole.data.event.data.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;
import ru.smole.OpPrison;
import ru.smole.data.event.data.Event;
import ru.smole.scoreboard.ScoreboardManager;
import ru.xfenilafs.core.scoreboard.BaseScoreboard;
import ru.xfenilafs.core.scoreboard.BaseScoreboardBuilder;
import ru.xfenilafs.core.util.Pair;
import ru.xfenilafs.core.util.Schedules;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class BlockEvent implements Event {

    public String id;
    public String[] description;
    public String name;
    public Function<BlockEvent, Runnable> runnable;
    public Consumer<BlockBreakEvent> consumer;

    private BukkitTask[] tasks;
    private Consumer<BaseScoreboardBuilder> consumerB;

    private BlockEvent(String id, String[] description, String name, Consumer<BlockBreakEvent> consumer) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.consumer = consumer;
        this.tasks = new BukkitTask[]{null};
    }

    public static BlockEvent get() {
        return new BlockEvent(null, null, null, null);
    }

    public BlockEvent id(String id) {
        setId(id);
        return this;
    }

    public BlockEvent description(String... desc) {
        setDescription(desc);
        return this;
    }

    public BlockEvent name(String name) {
        setName(name);
        return this;
    }

    public BlockEvent runnable(Function<BlockEvent, Runnable> runnable) {
        setRunnable(runnable);
        return this;
    }

    public BlockEvent consumer(Consumer<BlockBreakEvent> consumer) {
        setConsumer(consumer);
        return this;
    }

    public BlockEvent start() {
        if (id == null)
            throw new NullPointerException(this.getClass().getName() + ": id is null");

        if (name == null)
            throw new NullPointerException(this.getClass().getName() + ": name is null");

        Event.getEventManager().start(1, this);
        return this;
    }

    public BlockEvent start(int time) {
        if (id == null)
            throw new NullPointerException(this.getClass().getName() + ": id is null");

        if (name == null)
            throw new NullPointerException(this.getClass().getName() + ": name is null");

        Event.getEventManager().start(1, this);

        if (runnable != null)
            tasks[0] = runnable.andThen(runnable1 -> Schedules.runAsync(runnable1, time)).apply(this);

        OpPrison.getInstance().getScoreboardManager()
                .line(4, "§fСобытие:")
                .line(3, "§7✧ §e" + name.replace("&", "§"))
                .line(2, "")
                .build();

        return this;
    }

    public void stop() {
        Event.getEventManager().stop(1, this);

        if (!Bukkit.getOnlinePlayers().isEmpty())
            Bukkit.getOnlinePlayers().forEach(player -> {
                OpPrison.getInstance()
                        .getScoreboardManager()
                        .removeLine(4)
                        .removeLine(3)
                        .reloadScoreboard();
            });

        if (tasks.length != 0)
        Arrays.stream(tasks).forEach(bukkitTask -> {
            if (bukkitTask.isCancelled())
                bukkitTask.cancel();
        });
    }
}
