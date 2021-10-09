package ru.smole.mines;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ru.smole.OpPrison;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.regions.ResourceBlock;
import ru.xfenilafs.core.util.cuboid.BlockVector3;
import ru.xfenilafs.core.util.cuboid.Cuboid;
import ru.xfenilafs.core.util.temporal.TemporalUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class Mine {

    protected static final MineFillExecutor fillExecutor = new MineFillExecutor(OpPrison.getInstance(), 2);
    protected static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private final int blocksForFill;
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
        blocksForFill = zone.getSizeX() * zone.getSizeY() * zone.getSizeZ() / 6;
        blocks.sort(Comparator.comparingInt(ResourceBlock::getChance));
    }

    public void reset() {
        if (region == null) return;

        long current = System.currentTimeMillis();
        if (current - lastUpdate < respawn)
            return;
        lastUpdate = current;

        Bukkit.getOnlinePlayers().parallelStream()
                .filter(zone::contains)
                .forEach(player -> player.teleport(region.getSpawnLocation()));


        AtomicReference<Map<Block, ResourceBlock>> fillMap = new AtomicReference<>(new HashMap<>());
        AtomicInteger counter = new AtomicInteger();
        zone.forEach(block -> {
            if (counter.getAndIncrement() >= blocksForFill) {
                fillExecutor.post(fillMap.get());
                counter.set(0);
                fillMap.set(new HashMap<>());
            }

            if (block.getType() != Material.AIR) return;

            for (int i = 0; i < blocks.size(); i++)
                if (random.nextInt(101) <= blocks.get(i).getChance() || i == blocks.size() - 1)
                    fillMap.get().put(block, blocks.get(i));
        });
        fillExecutor.post(fillMap.get());
    }
}