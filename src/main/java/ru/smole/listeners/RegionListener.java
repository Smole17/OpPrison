package ru.smole.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.event.EventManager;
import ru.smole.data.event.data.Event;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.pvpcd.PvPCooldown;
import ru.smole.mines.Mine;
import ru.xfenilafs.core.regions.Region;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static ru.smole.OpPrison.*;

public class RegionListener implements Listener {

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Predicate<Mine> predicate = mine -> mine.getZone().contains(block);
        boolean noneMine = MINES.values().stream().noneMatch(predicate);
        if (noneMine) {
            if (!BUILD_MODE.contains(event.getPlayer())) {
                event.setCancelled(true);
            }

            return;
        }

        add(event);
        event.setExpToDrop(0);
        event.setDropItems(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!BUILD_MODE.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.DROWNING || event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishing(PlayerFishEvent event) {
        switch (event.getState()) {
            case CAUGHT_FISH: {
                event.setCancelled(true);
                event.setExpToDrop(0);
                break;
            }

            case FISHING:
                Region region = REGIONS.values().stream().filter(rg -> rg.getZone().contains(event.getPlayer())).findAny().orElse(null);
                if (region == null)
                    return;

                if (!region.isPvp())
                    event.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Player player = null;
        Entity damager = event.getDamager();
        if (damager.getType() == EntityType.PLAYER) {
            player = (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }

        if (entity.getType() != EntityType.PLAYER || player == null) {
            return;
        }

        if (REGIONS.values().stream().noneMatch(region -> region.getZone().contains(entity))) {
            return;
        }

        Region region = REGIONS.values().stream().filter(rg -> rg.getZone().contains(entity)).findAny().orElse(null);
        if (region == null) {
            return;
        }

        String entityName = entity.getName();
        String damagerName = player.getName();

        GangDataManager dataManager = OpPrison.getInstance().getGangDataManager();

        if (!region.isPvp() || dataManager.playerInGang(dataManager.getGangFromPlayer(damagerName), entityName)) {
            event.setCancelled(true);
            return;
        }

        PvPCooldown pvp = OpPrison.getInstance().getPvPCooldown();
        pvp.addPlayer(player);
        pvp.addPlayer((Player) entity);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Collection<Region> regions = REGIONS.values();
        if (regions.isEmpty()) {
            return;
        }
        event.blockList().removeIf(block -> regions.stream().anyMatch(region -> region.getZone().contains(block)));
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Collection<Region> regions = REGIONS.values();
        if (regions.isEmpty()) {
            return;
        }
        if (regions.stream().anyMatch(region -> region.getZone().contains(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Collection<Region> regions = REGIONS.values();
        if (regions.isEmpty()) {
            return;
        }
        event.blockList().removeIf(block -> regions.stream().anyMatch(region -> region.getZone().contains(block)));
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getNewState().getBlock().getType() == Material.SOIL) {
            event.setCancelled(true);
        }
    }

    public void add(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        Block block = event.getBlock();

        if (block.getType() == Material.SPONGE) {
            ItemStack itemStack = null;
            switch (ThreadLocalRandom.current().nextInt(0, 6)) {
                case 0:
                case 1:
                case 2:
                    itemStack = Items.getItem("sponge", 50.0);
                    break;
                case 3:
                case 4:
                    itemStack = Items.getItem("sponge", 100.0);
                    break;
                case 5:
                    itemStack = Items.getItem("sponge", 150.0);
                    break;
            }

            if (itemStack == null) return;

            OpPlayer.add(player, itemStack.clone());
            return;
        }

        if (block.getType() == Material.CHEST) {
            event.setCancelled(true);
            return;
        }

        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);
        pickaxe.procUpgrades(event, random);
        Event.getEventManager().blockBreak(event);
    }
}