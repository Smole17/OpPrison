package ru.smole.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.smole.OpPrison;

import java.util.Arrays;

public class ChatUtil {

    public static BukkitTask task;

    public static void sendTaskedMessage(Player player, int timeout, String... messages) {
        int[] i = {0};

        task = Bukkit.getScheduler().runTaskTimer(
                OpPrison.getInstance(),
                () -> {
                    try {
                        ru.xfenilafs.core.util.ChatUtil.sendMessage(player, messages[i[0]]);
                        i[0]++;
                    } catch (Exception ex) {
                        task.cancel();
                    }
                },
                20L * timeout,
                20L * timeout
        );
    }
}
