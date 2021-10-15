package ru.smole.listeners;

import com.google.common.collect.Lists;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import ru.smole.data.event.OpEvents;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.crates.CrateItem;
import ru.smole.data.npc.NpcInitializer;
import ru.smole.data.pads.LaunchPad;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.player.PlayerDataManager;
import ru.smole.guis.CaseLootGui;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.player.world.WorldStatisticPostLoadEvent;
import ru.xfenilafs.core.protocollib.entity.FakeBaseEntity;
import ru.xfenilafs.core.protocollib.entity.FakeEntityRegistry;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {

    private final PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @EventHandler
    public void onJoin(WorldStatisticPostLoadEvent event) {
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
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
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
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(OpPrison.getInstance(), () -> {
            LeaderBoard.holograms.forEach(holographic -> {

                boolean equalWorld = holographic.getLocation().getWorld().equals(player.getWorld());

                if (equalWorld) {
                    holographic.addViewers(player);
                    return;
                }

                holographic.removeViewers(player);
            });

            NpcInitializer.npcList.forEach(fakePlayer -> {
                boolean equalWorld = fakePlayer.getLocation().getWorld().equals(player.getWorld());

                if (equalWorld) {
                    fakePlayer.addViewers(player);
                    return;
                }

                fakePlayer.removeViewers(player);
            });
        }, 10L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        OpEvents.asyncChat(event);
        sendChat(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        if (event.hasBlock()) {
            Material type = block.getType();

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
                        return;

                    if (!OpEvents.getBreakEvents().containsKey("Искатель сокровищ"))
                        return;

                    OpPrison.MINES.forEach((integer, mine) -> {
                        if (!mine.getZone().contains(block))
                            return;

                        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
                        int random = ThreadLocalRandom.current().nextInt(4);

                        switch (random) {
                            case 0:
                            case 1:
                                playerData.addMoney(100000000000000D);
                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "§a+100T$");
                                break;

                            case 2:
                                playerData.addToken(250000000000D);
                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "§e+25B⛃");
                                break;

                            case 3:
                                ItemStack itemStack = Items.getItem("sponge", 50);

                                if (itemStack == null)
                                    break;

                                itemStack = itemStack.clone();

                                OpPlayer.add(player, itemStack);
                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили новый предмет %s", itemStack.getItemMeta().getDisplayName());
                                break;
                        }
                    });

                    event.setCancelled(true);
                    break;
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
    public void onOpen(InventoryOpenEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getName());
        OpPlayer opPlayer = new OpPlayer(player);

        opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
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

        String guildName = rPlayer.getGuild() == null || rPlayer.getGuild().getTag() == null ? "" : String.format("§7<%s§7> ", rPlayer.getGuild().getTag());
        String prefix = rPlayer.getLongPrefix();

        String format = String.format("%s§8[§a%s§8] %s%s§7: §f",
                guildName,
                StringUtils.formatDouble(
                        StringUtils._fixDouble(0,
                        playerData.getPrestige()).length() <= 3 ? 0 : 2,
                        playerData.getPrestige()
                ),
                prefix.replace('&', '§'),
                rPlayer.getVisibleName()
        );

        List<String> lore = Lists.newArrayList(
                String.format("&fНик: &b%s %s", prefix, name),
                "&fПрестиж: &a" + StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getPrestige()).length() <= 3 ? 0 : 2, playerData.getPrestige()),
                "&fДобыто блоков: &e" + StringUtils._fixDouble(0, playerData.getBlocks()),
                "&fГруппа: &b" + playerData.getGroup().getName(),
                "",
                "&fДеньги: &a" + StringUtils.formatDouble(2, playerData.getMoney()),
                "&fТокенов: &e" + StringUtils.formatDouble(2, playerData.getToken()),
                "&fМножитель: &d" + StringUtils._fixDouble(0, playerData.getMultiplier())
        );
        BaseComponent[] comps = new BaseComponent[lore.size()];

        for (int i = 0; i < lore.size(); ++i) {
            comps[i] = new TextComponent(ChatUtil.color(String.format("%s%s", lore.get(i), i == lore.size() - 1 ? "" : "\n")));
        }

        TextComponent _component = new TextComponent(format.replaceFirst("!", ""));
        _component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, comps));
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
                            : Bukkit.getServer().getItemFactory().getItemMeta(item.getType()).getDisplayName(), text_peace);

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
