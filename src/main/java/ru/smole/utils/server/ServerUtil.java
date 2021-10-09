package ru.smole.utils.server;

import org.bukkit.Bukkit;
import ru.luvas.rmcs.utils.LagMeter;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerDataManager;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Random;

public class ServerUtil {

    public static void load() {
        Task.schedule(new Runnable() {

            public void run() {
                Bukkit.getWorlds().forEach(world -> world.setTime(18000L));
                Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerTime(1200L, false));
                if (LagMeter.getLastMinuteTPS() < 5.0D) {
                    Bukkit.getOnlinePlayers().forEach(player -> ChatUtil.sendMessage(player, "&4&lСожалеем, но сервер крайне перегружен. Попробуйте вернуться через несколько минут."));
                    Bukkit.getOnlinePlayers().forEach(player -> BungeeUtil.sendToServer(player, "hub"));
                }
            }
        }, 0L, 20L);
    }

    public static void restart() {
        Random random = new Random();
        Bukkit.getOnlinePlayers().forEach((player) -> {
            int randomI = random.nextInt(2);
            ChatUtil.sendMessage(player, "§fСервер перезагрузится через §c10 §fсекунд. Рекомендуем покинуть его и войти через минуту.");

            BungeeUtil.sendToServer(player, "hub" + randomI +1);
            PlayerDataManager playerData = OpPrison.getInstance().getPlayerDataManager();
            playerData.unload(player);
        });

        Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), Bukkit::shutdown, 200L);
    }
}