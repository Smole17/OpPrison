package ru.smole.mines;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import ru.smole.OpPrison;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.regions.ResourceBlock;
import ru.xfenilafs.core.util.cuboid.BlockVector3;
import ru.xfenilafs.core.util.cuboid.Cuboid;
import ru.xfenilafs.core.util.temporal.TemporalUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Getter
public class Mine {

    private final int level;
    private final Region region;
    private final Cuboid zone;
    private final long respawn;
    private final List<ResourceBlock> blocks;

    private long lastUpdate;

    public Mine(int level, String region, String world, String minPoint, String maxPoint, String resetTime, List<ResourceBlock> blocks) {
        this.level = level;
        this.region = OpPrison.REGIONS.get(region.toLowerCase());
        String[] minPointSplit = minPoint.split(" ");
        String[] maxPointSplit = maxPoint.split(" ");
        this.zone = Cuboid.fromWorldAndPoints(
                Bukkit.getWorld(world),
                BlockVector3.at(
                        Integer.parseInt(minPointSplit[0]),
                        Integer.parseInt(minPointSplit[1]),
                        Integer.parseInt(minPointSplit[2])
                ),
                BlockVector3.at(
                        Integer.parseInt(maxPointSplit[0]),
                        Integer.parseInt(maxPointSplit[1]),
                        Integer.parseInt(maxPointSplit[2])
                )
        );
        this.respawn = TemporalUtils.parseTemporal(resetTime).get(TimeUnit.MILLISECONDS);
        this.blocks = blocks;
        reset();
    }

    public void reset() {
        if (region == null) {
            return;
        }
        long current = System.currentTimeMillis();
        if (current - lastUpdate < respawn) {
            return;
        }
        lastUpdate = current;
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (zone.contains(player)) {
                player.teleport(region.getSpawnLocation());
            }
        });
        blocks.sort(Comparator.comparingInt(ResourceBlock::getChance));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        zone.forEach(block -> {
            ResourceBlock resource = null;
            while (resource == null) {
                for (ResourceBlock resourceBlock : blocks) {
                    if (random.nextInt(100) <= resourceBlock.getChance()) {
                        resource = resourceBlock;
                        break;
                    }
                }
            }
            Location location = block.getLocation();
            Block blockAt = location.getWorld().getBlockAt(location);
            blockAt.setType(resource.getType());
        });
    }
}