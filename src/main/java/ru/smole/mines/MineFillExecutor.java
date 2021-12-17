package ru.smole.mines;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.xfenilafs.core.regions.ResourceBlock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MineFillExecutor extends BukkitRunnable {

    private final Queue<Map<Block, ResourceBlock>> queue;
    private final Plugin plugin;
    private final int delay;

    public MineFillExecutor(Plugin plugin, int delay) {
        this.plugin = plugin;
        this.delay = delay;
        queue = new LinkedList<>();
        run();
    }

    @SuppressWarnings("ALL")
    public void run() {
        Map<Block, ResourceBlock> fillMap = queue.poll();
        if (fillMap != null)
            fillMap.forEach((block, data) -> setTypeIdAndData(block, data.getType().getId(), data.getData()));
        Bukkit.getScheduler().runTaskLater(plugin, this, delay);
    }

    public void post(Map<Block, ResourceBlock> fillMap) {
        if (!fillMap.isEmpty()) queue.add(fillMap);
    }

//    @SuppressWarnings("ALL")
//    public void setBlockTypeAndData(Block block, Material type, int data) {
////        block.setTypeIdAndData(type.getId(), data, false);
//
//        IBlockData blockData = CraftMagicNumbers.getBlock(type).fromLegacyData(data);
//        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
//
//        if (block.getType() == type || data == block.getData())
//            return;
//
//        World world = ((CraftWorld) block.getWorld()).getHandle();
//        Chunk chunk = world.getChunkAtWorldCoords(position);
//
//        IBlockData oldBlockData = chunk.getBlockData(position);
//
//        CraftBlockState state = CraftBlockState.getBlockState(world, position.getX(), position.getY(), position.getZ(), 18);
//        state.setTypeId(CraftMagicNumbers.getId(blockData.getBlock()));
//        state.setRawData((byte) blockData.getBlock().toLegacyData(blockData));
//
//        Iterator<BlockState> states = world.capturedBlockStates.iterator();
//        BlockState rm;
//        while (states.hasNext()) {
//            rm = states.next();
//            if (rm.getX() == state.getX() && rm.getY() == state.getY() && rm.getZ() == state.getZ()) {
//                states.remove();
//                break;
//            }
//        }
//
//        world.capturedBlockStates.add(state);
//
//        chunk.getWorld().notify(position, oldBlockData, blockData, 3);
//
//    }

    public boolean setTypeIdAndData(Block block, int type, int data) {
        CraftChunk chunk = ((CraftChunk) block.getChunk());
        IBlockData blockData = CraftMagicNumbers.getBlock(type).fromLegacyData(data);
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        if (type != 0 && blockData.getBlock() instanceof BlockTileEntity && type != block.getTypeId()) {
            chunk.getHandle().getWorld().setTypeAndData(position, Blocks.AIR.getBlockData(), 0);
        }

        IBlockData old = chunk.getHandle().getBlockData(position);
        boolean success = setTypeAndData(chunk, chunk.getHandle().getWorld(), position, blockData, 18);
        if (success)
            chunk.getHandle().getWorld().notify(position, old, blockData, 3);

        return success;
    }

    public boolean setTypeAndData(CraftChunk chunk, World world, BlockPosition blockposition, IBlockData iblockdata, int i) {
        if (world.captureTreeGeneration) {
            BlockState blockstate = null;
            Iterator it = world.capturedBlockStates.iterator();

            while (it.hasNext()) {
                BlockState previous = (BlockState) it.next();
                if (previous.getX() == blockposition.getX() && previous.getY() == blockposition.getY() && previous.getZ() == blockposition.getZ()) {
                    blockstate = previous;
                    it.remove();
                    break;
                }
            }

            if (blockstate == null)
                blockstate = CraftBlockState.getBlockState(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), i);

            blockstate.setTypeId(CraftMagicNumbers.getId(iblockdata.getBlock()));
            blockstate.setRawData((byte) iblockdata.getBlock().toLegacyData(iblockdata));
            world.capturedBlockStates.add(blockstate);
            return true;
        } else if (blockposition.getY() < 0 || blockposition.getY() >= 256) return false;
        else if (world.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) return false;
        else {
            iblockdata.getBlock();
            BlockState blockstate = null;
            if (world.captureBlockStates) {
                blockstate = CraftBlockState.getBlockState(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), i);
                world.capturedBlockStates.add(blockstate);
            }

            IBlockData iblockdata1 = a(chunk.getHandle(), blockposition, iblockdata);
            if (iblockdata1 == null) {
                if (world.captureBlockStates)
                    world.capturedBlockStates.remove(blockstate);
                return false;
            } else {
                if (!world.captureBlockStates)
                    world.notifyAndUpdatePhysics(blockposition, chunk.getHandle(), iblockdata1, iblockdata, i);
                return true;
            }
        }
    }

    public IBlockData a(Chunk chunk, BlockPosition blockposition, IBlockData iblockdata) {
        int i = blockposition.getX() & 15;
        int j = blockposition.getY();
        int k = blockposition.getZ() & 15;
        int l = k << 4 | i;

        int i1 = chunk.heightMap[l];
        IBlockData iblockdata1 = chunk.getBlockData(blockposition);
        if (iblockdata1 == iblockdata) return null;
        else {
            net.minecraft.server.v1_12_R1.Block block = iblockdata.getBlock();
            net.minecraft.server.v1_12_R1.Block block1 = iblockdata1.getBlock();
            ChunkSection chunksection = chunk.getSections()[j >> 4];
            boolean flag = false;
            if (chunksection == null) {
                if (block == Blocks.AIR) return null;
                chunksection = new ChunkSection(j >> 4 << 4, chunk.world.worldProvider.m());
                chunk.getSections()[j >> 4] = chunksection;
                flag = j >= i1;
            }

            chunksection.setType(i, j & 15, k, iblockdata);
            if (block1 != block)
                block1.remove(chunk.world, blockposition, iblockdata1);


            if (chunksection.getType(i, j & 15, k).getBlock() != block) return null;

            TileEntity tileentity;
            if (block1 instanceof ITileEntity) {
                tileentity = chunk.a(blockposition, Chunk.EnumTileEntityState.CHECK);
                if (tileentity != null) tileentity.invalidateBlockCache();
            }

            if (block1 != block && (!chunk.world.captureBlockStates || block instanceof BlockTileEntity))
                block.onPlace(chunk.world, blockposition, iblockdata);

            if (block instanceof ITileEntity) {
                tileentity = chunk.a(blockposition, Chunk.EnumTileEntityState.CHECK);
                if (tileentity == null) {
                    tileentity = ((ITileEntity) block).a(chunk.world, block.toLegacyData(iblockdata));
                    chunk.world.setTileEntity(blockposition, tileentity);
                }

                if (tileentity != null) tileentity.invalidateBlockCache();
            }

            return iblockdata1;
        }
    }

}
