package ru.smole.data.gang.point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.smole.OpPrison;
import ru.smole.data.event.OpEvents;
import ru.smole.data.items.Items;
import ru.smole.data.player.OpPlayer;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.Schedules;
import ru.xfenilafs.core.util.cuboid.Cuboid;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class PointEvent {

    private final @Getter Collection<Region> regionList;
    private BukkitTask task;

    public PointEvent(Collection<Region> regionList) {
        this.regionList = regionList;
    }

    public void start() {
        OpEvents.start("point");
        task = Schedules.runAsync(() -> {
            regionList
                    .stream()
                    .parallel()
                    .filter(region -> region.getName().contains("point_"))
                    .forEach(region -> {
                        Cuboid zone = region.getZone();

                        Bukkit.getOnlinePlayers().
                                stream()
                                .parallel()
                                .forEach(player -> {
                                    if (zone.contains(player)) {
                                        OpPlayer.add(player, Items.getItem("sponge", 50.0));
                                    }
                                });
                    });
        }, 20, 20);
    }

    public void stop() {
        OpEvents.stop("point");

        if (task != null && !task.isCancelled())
            task.cancel();
    }
}
