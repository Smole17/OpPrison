package ru.smole.utils;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import ru.luvas.rmcs.utils.LagMeter;
import ru.smole.OpPrison;
import ru.smole.data.PlayerDataManager;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ServerUtil {

    public static void load() {
        Task.schedule(new Runnable() {
            private long time = 0L;

            public void run() {
                this.time += 24L;
                this.time %= 86400L;
                int trueTime = (int)(this.time / 86400.0D * 24000.0D);
                Bukkit.getWorlds().forEach(world -> world.setTime(18000L));
                Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerTime(trueTime, false));
                if (LagMeter.getLastMinuteTPS() < 5.0D) {
                    Bukkit.getOnlinePlayers().forEach(player -> ChatUtil.sendMessage(player, "&4&lСожалеем, но сервер крайне перегружен. Попробуйте вернуться через несколько минут."));
                    Bukkit.getOnlinePlayers().forEach(player -> BungeeUtil.sendToServer(player, "hub"));
                }
            }
        }, 0L, 20L);
    }

    public static void restart() {
        Bukkit.getOnlinePlayers().forEach((player) -> {
            ChatUtil.sendMessage(player, "§fСервер перезагрузится через §c10 §fсекунд. Рекомендуем покинуть его и войти через минуту.");
            PlayerDataManager playerData = OpPrison.getInstance().getPlayerDataManager();
            playerData.unload(player);
        });
        Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach((p) -> {
                ChatUtil.sendMessage(p, "§cСервер перезагружается! Перемещаю в хаб проекта...");
                BungeeUtil.sendToServer(p, "hub");
            });
        }, 200L);
        Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), Bukkit::shutdown, 200L);
    }
}