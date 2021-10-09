package ru.smole.mines;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.xfenilafs.core.regions.ResourceBlock;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MineFillExecutor extends BukkitRunnable {

    private Queue<Map<Block, ResourceBlock>> queue;
    
    public MineFillExecutor(Plugin plugin, int delay) {
        queue = new LinkedList<>();
        runTaskTimer(plugin, 20, delay);
    }

    public void run() {
        Map<Block, ResourceBlock> fillMap = queue.peek();
        if (fillMap != null)
            fillMap.forEach((block, data) -> block.setTypeIdAndData(data.getType().getId(), (byte) data.getData(), false));
    }

    public void post(Map<Block, ResourceBlock> fillMap) {
        if (!fillMap.isEmpty()) queue.add(fillMap);
    }

}
