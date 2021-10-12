package ru.smole.utils.server;

import org.bukkit.Bukkit;
import ru.luvas.rmcs.utils.LagMeter;
import ru.luvas.rmcs.utils.UtilBungee;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerDataManager;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ServerUtil {

    public static void load() {
        Task.schedule(new Runnable() {

            public void run() {
                Bukkit.getWorlds().forEach(world -> world.setTime(18000L));
                Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerTime(1200L, false));
                if (LagMeter.getLastMinuteTPS() < 5.0D) {
                    Bukkit.getOnlinePlayers().forEach(player -> ChatUtil.sendMessage(player, "&4&lСожалеем, но сервер крайне перегружен. Попробуйте вернуться через несколько минут."));
                }
            }
        }, 0L, 20L);
    }

    public static void restart() {
        Bukkit.getOnlinePlayers().forEach((player) -> {
            ChatUtil.sendMessage(player, "§fСервер перезагрузится через §c10 §fсекунд. Рекомендуем покинуть его и войти через минуту.");

            PlayerDataManager playerData = OpPrison.getInstance().getPlayerDataManager();
            playerData.unload(player);
            UtilBungee.sendPlayer(player, "hub" + ThreadLocalRandom.current().nextInt(1, 3));
        });

        Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), Bukkit::shutdown, 200L);
    }
}