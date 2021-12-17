package ru.smole.data.event.impl;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.event.Event;
import ru.smole.data.items.Items;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.config.ConfigUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.holographic.ProtocolHolographic;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.Schedules;
import ru.xfenilafs.core.util.cuboid.Cuboid;
import sexy.kostya.mineos.achievements.Achievement;

import java.time.ZoneId;
import java.util.*;

public class PointEvent {

    public static final Collection<ProtocolHolographic> holograms = new ArrayList<>();
    private final @Getter Collection<Region> regionList;
    private BukkitTask task;
    private BukkitTask task2;

    public PointEvent(Collection<Region> regionList) {
        this.regionList = regionList;
    }

    public void start() {
        Event.getEventManager().getOtherEvents().put("point", null);

        task = Schedules.runAsync(() -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));
            if (calendar.get(Calendar.HOUR_OF_DAY) == 20 && Event.getEventManager().getOtherEvents().containsKey("point")) {
                stop();
                return;
            }

            regionList
                    .stream()
                    .parallel()
                    .filter(region -> region.getName().contains("point_"))
                    .forEach(region -> {
                        Cuboid zone = region.getZone();

                        Bukkit.getOnlinePlayers().stream()
                                .parallel()
                                .forEach(player -> {
                                    if (zone.contains(player)) {
                                        OpPlayer.add(player, Items.getItem("sponge", 50.0));
                                        RPlayer.checkAndGet(player.getName()).getAchievements().addAchievement(Achievement.OP_POINT_EVENT);
                                    }
                                });
                    });
        }, 20, 20);

        task2 = Schedules.runAsync(() -> {
            if (!Bukkit.getOnlinePlayers().isEmpty())
                Bukkit.getOnlinePlayers().stream().parallel().forEach(player -> ChatUtil.sendTitle(player, "§fЗахват Точек", "§aактивен", 15));

            ChatUtil.broadcast("");
            ChatUtil.broadcast("   &fСобытие Захват Точек §aактивно");
            ChatUtil.broadcast("   &fСуть события в захвате точек на бандитской арене,");
            ChatUtil.broadcast("   &fкоторые находятся на 3-х островах с шахтами");
            ChatUtil.broadcast("");
        }, 1, 20 * 300);

        OpPrison.REGIONS.forEach((name, region) -> {
            String s = region.getName();
            if (!s.contains("point_"))
                return;

            ProtocolHolographic point = ApiManager.createHolographic(
                    ConfigUtils.loadLocationFromConfigurationSection(
                            OpPrison.getInstance().getConfigManager().getMiscConfig().getConfiguration().getConfigurationSection(s)
                    )
            );

            point.addTextLine("§fТочка §b" + s.split("_")[1]);
            point.addTextLine("§8§o(стойте на точке, чтобы получать очки)");

            point.spawn();

            holograms.add(point);
        });
    }

    public void stop() {
        Event.getEventManager().stop(-1, "point");

        if (task != null && !task.isCancelled())
            task.cancel();

        if (task2 != null && !task2.isCancelled())
            task2.cancel();

        holograms.forEach(ProtocolHolographic::remove);
        holograms.clear();

        Bukkit.getOnlinePlayers().stream().parallel().forEach(player -> ChatUtil.sendTitle(player, "§fЗахват Точек", "§cокончилось", 15));
        ChatUtil.broadcast("");
        ChatUtil.broadcast("   &fСобытие Захват Точек §cокончилось");
        ChatUtil.broadcast("");
    }
}
