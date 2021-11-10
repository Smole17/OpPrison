package ru.smole.listeners;

import com.google.common.collect.Lists;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.cases.Case;
import ru.smole.data.event.EventManager;
import ru.smole.data.event.data.Event;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.event.data.impl.PointEvent;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.crates.CrateItem;
import ru.smole.data.pads.LaunchPad;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.player.PlayerDataManager;
import ru.smole.guis.CaseLootGui;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.ChatUtil;
import sexy.kostya.mineos.achievements.Achievement;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {

    private final PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        dataManager.load(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        dataManager.unload(player);
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();

        dataManager.unload(player);
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().contains("gangs")) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);

        Player player = event.getEntity();
        Player killer = player.getKiller();

        event.getDrops()
                .forEach(itemStack -> {
                    Material material = itemStack.getType();
                    if (
                            material == Material.FISHING_ROD
                            || material == Material.IRON_BOOTS
                            || material == Material.IRON_LEGGINGS
                            || material == Material.IRON_CHESTPLATE
                            || material == Material.IRON_HELMET
                            || material == Material.DIAMOND_BOOTS
                            || material == Material.DIAMOND_LEGGINGS
                            || material == Material.CHAINMAIL_BOOTS
                            || material == Material.CHAINMAIL_LEGGINGS
                            || material == Material.CHAINMAIL_CHESTPLATE
                            || material == Material.CHAINMAIL_HELMET
                            || material == Material.DIAMOND_CHESTPLATE
                            || material == Material.DIAMOND_HELMET
                            || material == Material.BOW
                                    || material == Material.POTION
                                    || material == Material.SPLASH_POTION
                            || material == Material.ARROW
                    ) {
                        player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                        itemStack.setAmount(0);
                    }
                });

        if (killer != null) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вас убил &a%s", killer.getName());
            ChatUtil.sendMessage(killer, OpPrison.PREFIX + "Вы убили &a%s", player.getName());
        }

        OpPrison.getInstance().getPvPCooldown().removePlayer(player, false);
        event.setDroppedExp(0);
        event.setDeathMessage("");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        event.setRespawnLocation(OpPrison.REGIONS.get("spawn").getSpawnLocation());
        LeaderBoard.holograms.forEach(simpleHolographic -> {
            if (!simpleHolographic.getLocation().getWorld().equals(player.getWorld()))
                return;

            simpleHolographic.spawn();
        });

        if (PointEvent.holograms.isEmpty())
            return;

        PointEvent.holograms.forEach(protocolHolographic -> {
            if (!protocolHolographic.getLocation().getWorld().equals(player.getWorld()))
                return;

            protocolHolographic.spawn();
        });
    }

//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onWorldChange(PlayerChangedWorldEvent event) {
//        Player player = event.getPlayer();
//        Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), () -> {
//            LeaderBoard.holograms.forEach(holographic -> {
//
//                boolean equalWorld = holographic.getLocation().getWorld().equals(player.getWorld());
//
//                if (equalWorld) {
//                    holographic.addViewers(player);
//                    return;
//                }
//
//                holographic.removeViewers(player);
//            });
//
//            NpcInitializer.npcList.forEach(fakePlayer -> {
//                boolean equalWorld = fakePlayer.getLocation().getWorld().equals(player.getWorld());
//
//                if (equalWorld) {
//                    fakePlayer.addViewers(player);
//                    return;
//                }
//
//                fakePlayer.removeViewers(player);
//            });
//        }, 10L);
//    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Event.getEventManager().asyncChat(event);
        sendChat(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        if (event.hasBlock()) {
            Material type = block.getType();

            if (type == Material.ANVIL) {
                event.setCancelled(true);
                return;
            }

            switch (action) {
                case LEFT_CLICK_BLOCK: {
                    if (event.hasBlock()) {
                        Case customCase = Case.getCustomCaseByLocation(block);

                        if (type == Material.CHEST && event.getHand() == EquipmentSlot.HAND && customCase != null) {
                            new CaseLootGui(customCase).openInventory(player);
                            event.setCancelled(true);
                        }
                    }

                    break;
                }

                case RIGHT_CLICK_BLOCK: {
                    if (type != Material.CHEST)
                        break;

                    if (!Event.getEventManager().getBlockEvents().containsKey("treasure")) {
                        event.setCancelled(true);
                        break;
                    }

                    if (player.getWorld().equals(Bukkit.getWorld("spawn"))) {
                        event.setCancelled(true);
                        break;
                    }

                    Location loc = block.getLocation();
                    if (!Event.getEventManager().getTreasureMap().containsKey(player.getName()) || !Event.getEventManager().getTreasureMap().get(player.getName()).contains(loc)) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Это не ваше сокровище!");
                        event.setCancelled(true);
                        break;
                    }


                    PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
                    int random = ThreadLocalRandom.current().nextInt(5);

                    switch (random) {
                        case 0:
                            ItemStack mine_key = Items.getItem("mine_key", 4.0);

                            if (mine_key == null)
                                break;

                            mine_key = mine_key.clone();

                            OpPlayer.add(player, mine_key);
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили новый предмет %s &fx4", mine_key.getItemMeta().getDisplayName());
                            break;

                        case 1:
                            ItemStack epic_key = Items.getItem("epic_key", 1.0);

                            if (epic_key == null)
                                break;

                            epic_key = epic_key.clone();

                            OpPlayer.add(player, epic_key);
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили новый предмет %s &fx1", epic_key.getItemMeta().getDisplayName());
                            break;

                        case 2:
                            double current = 100000000000D;
                            playerData.addMoney(current);
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "§6+$" + StringUtils.formatDouble(0, current));
                            break;

                        case 3:
                            double current1 = 25000000D;
                            playerData.addToken(current1);
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "§e+⛃" + StringUtils.formatDouble(0, current1));
                            break;

                        case 4:
                            ItemStack sponge = Items.getItem("sponge", 50.0);

                            if (sponge == null)
                                break;

                            sponge = sponge.clone();

                            OpPlayer.add(player, sponge);
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили новый предмет %s", sponge.getItemMeta().getDisplayName());
                            break;
                    }

                    block.getWorld().spawnParticle(Particle.CLOUD, loc, 7);
                    block.setType(Material.AIR);

                    RPlayer.checkAndGet(player.getName()).getAchievements().addAchievement(Achievement.OP_TAKE_TREASURE);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        Items.interact(event);
    }

    @EventHandler
    public void onSwitch(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        val crates = Crate.crates;

        if (!crates.isEmpty())
            crates.forEach((s, crate) -> {
                if (inv.getName().equals(crate.getType().getName())) {
                    Player player = Bukkit.getPlayer(event.getPlayer().getName());
                    List<CrateItem> items = Crate.items;
                    OpPlayer opPlayer = new OpPlayer(player);

                    if (!items.isEmpty()) {
                        items.forEach(crateItem -> {
                            ItemStack itemStack = crateItem.get(player.getName());

                            if (crateItem.getType() == CrateItem.CrateItemType.ITEM)
                                opPlayer.add(itemStack);

                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили недостающий предмет %s", itemStack.getItemMeta().getDisplayName());
                        });

                        items.clear();
                    }
                }
            });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = Bukkit.getPlayer(event.getWhoClicked().getName());

        if(event.getHotbarButton() != -1) {
            ItemStack item = player.getInventory().getContents()[event.getHotbarButton()];
            if(item != null)
                if (Items.isSomePickaxe(item, player.getName()))
                    event.setCancelled(true);
                return;
        }

        InventoryAction action = event.getAction();

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.CLONE_STACK || action == InventoryAction.COLLECT_TO_CURSOR) {
            if (event.getInventory().getName() != null && event.getInventory().getName().contains("Хранилище"))
                if (Items.isSomePickaxe(event.getCurrentItem(), player.getName()) || event.getCurrentItem().getType() == Material.NETHER_STAR)
                    event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (Items.isSomePickaxe(event.getCursor(), event.getWhoClicked().getName()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
        ChatUtil.sendMessage(event.getPlayer(), OpPrison.PREFIX + "§fВы не можете выбрасывать предметы. Используйте §a/trash");
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = event.getTo().getBlock();

        if (block.getY() <= 10) {
            Optional<Region> findRegion = OpPrison.REGIONS.values()
                    .stream()
                    .filter(region -> region.getZone().contains(block))
                    .findAny();

            if (findRegion.isPresent()) {
                player.teleport(findRegion.get().getSpawnLocation());
                return;
            }

            player.teleport(OpPrison.REGIONS.get("spawn").getSpawnLocation());
            return;
        }

        Optional<LaunchPad> findPad = OpPrison.PADS
                .stream()
                .filter(launchPad -> launchPad.getLocation().getBlock().equals(block))
                .findAny();

        findPad.ifPresent(launchPad -> launchPad.launch(player));
    }

    public void sendChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        String name = player.getName();
        String msg = event.getMessage();
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        RPlayer rPlayer = RPlayer.checkAndGet(name);

        String guildName = rPlayer.getColoredTag();
        String prefix = rPlayer.getLongPrefix();

        String format = String.format("%s §8[§a%s§8] %s%s§7: §f",
                guildName.replace('&', '§'),
                StringUtils.formatDouble(
                        StringUtils._fixDouble(0,
                        playerData.getPrestige()).length() <= 3 ? 0 : 2,
                        playerData.getPrestige()
                ),
                prefix.replace('&', '§'),
                rPlayer.getVisibleName()
        );

        GangDataManager gManager = OpPrison.getInstance().getGangDataManager();
        List<String> lore = Lists.newArrayList(
                String.format("&fНик: &b%s %s", prefix, name),
                "&fПрестиж: &a" + StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getPrestige()).length() <= 3 ? 0 : 2, playerData.getPrestige()),
                "&fДобыто блоков: &e" + StringUtils._fixDouble(0, playerData.getBlocks()),
                "&fГруппа: &b" + playerData.getGroup().getName(),
                "&fБанда: &f" + (gManager.playerHasGang(name) ? gManager.getGangFromPlayer(name).getName() : "&c-"),
                "",
                "&fДеньги: &a" + StringUtils.formatDouble(2, playerData.getMoney()),
                "&fТокенов: &e" + StringUtils.formatDouble(2, playerData.getToken()),
                "&fМножитель: &d" + StringUtils._fixDouble(0, playerData.getMultiplier())
        );
        BaseComponent[] comps = new BaseComponent[lore.size()];

        for (int i = 0; i < lore.size(); ++i) {
            comps[i] = new TextComponent(ChatUtil.color(String.format("%s%s", lore.get(i), i == lore.size() - 1 ? "" : "\n")));
        }

        TextComponent _component = new TextComponent(format);
        _component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, comps));
        _component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ChatUtil.text("/m %s ", player.getName())));
        _component.addExtra(getHand(msg, item));

        event.setCancelled(true);
        Bukkit.getOnlinePlayers().forEach(players -> players.spigot().sendMessage(_component));

        Bukkit.getConsoleSender().sendMessage(format + msg);
    }


    public TextComponent getHand(String msg, ItemStack item) {
        if (item == null)
            return new TextComponent(msg);

        if (item.getType() == Material.AIR)
            return new TextComponent(msg);

        if (msg.equals("#рука") || msg.equals("#hand") || msg.equals("[item]")) {
            int amount = item.getAmount();
            String text_peace = amount == 1 ? "" : " §fx" + amount;
            String text = String.format("§8[§f%s%s§8]",
                    item.hasItemMeta() ? item.getItemMeta().getDisplayName()
                            : item.getType().name().replace("_", ""), text_peace);

//            msg = text;
//
//            ItemMeta meta = item.getItemMeta();
//            if (meta == null)
//                return new TextComponent(msg);
//
//            StringBuilder show = new StringBuilder();
//            List<String> lore = meta.getLore();
//
//            if (lore != null && !lore.isEmpty()) {
//                for (String s : lore) {
//                    show.append("\n").append(s);
//                }
//            }

            BaseComponent[] itemComponent = ChatUtil.newBuilder()
                    .setText(text)
                    .setHoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackUtils.convertItemStackToJsonRegular(item))
                    .build();

            return new TextComponent(itemComponent);
        }

        return new TextComponent(msg);
    }
}
