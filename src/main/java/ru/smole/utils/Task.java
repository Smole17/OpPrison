package ru.smole.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.*;
import java.util.*;
import org.bukkit.plugin.java.*;
import org.bukkit.*;
import ru.smole.OpPrison;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Task extends BukkitRunnable {
    public static final HashMap<String, Task> tasks = new HashMap<>();

    @Getter @Setter
    private int periods;
    @Getter
    private int delay;
    @Getter
    private int period;
    @Getter
    private final String name;
    @Getter
    private final JavaPlugin plugin;

    public Task(JavaPlugin plugin, String name, int delayInMilliseconds, int periodInMilliseconds) {
        if (delayInMilliseconds != 0 && delayInMilliseconds < 50) {
            throw new IllegalArgumentException("Delay time must be 0 or not less than 50ms!");
        }
        if (periodInMilliseconds < 50) {
            throw new IllegalArgumentException("Period time must be not less than 50ms!");
        }
        this.name = name;
        this.plugin = plugin;
        this.delay = delayInMilliseconds / 50;
        this.period = periodInMilliseconds / 50;
        runTaskTimer(plugin, delay, period);
        Task.tasks.put(name, this);
    }

    public abstract void onTick();

    public void run() {
        if (this.periods > 0) {
            --this.periods;
        }
        this.onTick();
        if (this.periods == 0) {
            this.cancel();
        }
    }

    public void cancel() {
        super.cancel();
        Task.tasks.remove(getName());
    }

    public static Task getTask(String name) {
        return Task.tasks.get(name);
    }

    public static void schedule(Runnable r) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(OpPrison.getInstance(), r);
    }

    public static void schedule(Runnable r, long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(OpPrison.getInstance(), r, delay);
    }

    public static void schedule(Runnable r, long delay, long period) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(OpPrison.getInstance(), r, delay, period);
    }
}
