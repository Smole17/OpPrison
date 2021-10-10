package ru.smole.data.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.apache.logging.log4j.util.BiConsumer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.luvas.rmcs.MainClass;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.cases.Case;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.guis.PickaxeGui;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.player.CorePlayer;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.ItemUtil;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.achievements.Achievements;

import java.util.*;
import java.util.function.Function;

public class Items {

    private static @Getter
    Map<String, Function<Object[], ItemStack>> creators;
    private static @Getter
    Map<String, BiConsumer<PlayerInteractEvent, ItemStack>> interacts;

    public static ItemStack getItem(String name, Object... params) {
        Function<Object[], ItemStack> creator = creators.get(name.toLowerCase());
        if (creator == null)
            return null;
        return setTagName(creator.apply(params), name.toLowerCase());
    }

    public static String getItemName(ItemStack item) {
        if (item != null) {
            NBTTagString nbt = ItemStackUtils.getTag(ApiManager.newItemBuilder(item), "op_item");
            String name;
            if (nbt != null && (name = nbt.c_()) != null) {
                if (creators.containsKey(name.toLowerCase()))
                    return name;
            }
        }
        return "";
    }

    protected static ItemStack getPickaxe(String name) {
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(name);

        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§b§lЗачарования");
        for (Upgrade upgrade : Upgrade.values()) {

            double count = pickaxe
                    .getUpgrades()
                    .get(upgrade)
                    .getCount();

            if (count == 0)
                continue;

            lore.add(String.format("%s| %s %s", ChatColor.getLastColors(upgrade.getName()), upgrade.getName(), StringUtils._fixDouble(0, count)));
        }
        lore.add("");
        lore.add("§b§lСтатистика");
        lore.add(String.format("§b| §fУровень: §b%s", (int) pickaxe.getLevel()));
        lore.add(String.format("§b| §fОпыт: §b%s/%s", (int) pickaxe.getExp(), (int) pickaxe.getNeedExp()));

        double efficiency = pickaxe.getUpgrades().get(Upgrade.EFFICIENCY).getCount();

        ItemUtil.ItemBuilder itemPick = ApiManager.newItemBuilder(Material.DIAMOND_PICKAXE)
                .setUnbreakable(true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addItemFlag(ItemFlag.HIDE_DESTROYS)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setName("§fКирка " + pickaxe.getName())
                .setLore(lore);

        return pickaxe.getUpgrades()
                .get(Upgrade.EFFICIENCY)
                .isIs()
                ? itemPick.addEnchantment(Enchantment.DIG_SPEED, (int) efficiency).build()
                : itemPick.build();
    }

    public static boolean isSomePickaxe(ItemStack itemStack, String playerName) {
        if (itemStack != null)
            if (itemStack.hasItemMeta()) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta.hasDisplayName())
                    if (itemMeta.getDisplayName().contains(playerName))
                        return itemStack.getType() == Material.DIAMOND_PICKAXE;
            }

        return false;
    }

    // register items

    public static void init() {
        creators = new HashMap<>();
        interacts = new HashMap<>();
        Map<String, PlayerData> playerDataMap = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap();

        registerItem("pickaxe",
                objects -> getPickaxe(objects[0].toString()),
                (playerInteractEvent, itemStack) -> {
                    Action action = playerInteractEvent.getAction();
                    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                        new PickaxeGui().openInventory(playerInteractEvent.getPlayer());
                 }
        );

        registerItem("fly", objects ->
                ApiManager
                        .newItemBuilder(Material.FEATHER)
                        .setName("§fДоступ к полёту §8/fly)")
                        .addLore("§7Нажмите для активации")
                        .setAmount(((Double) objects[0]).intValue())
                        .build(),
                (playerInteractEvent, itemStack) -> {
                    Player player = playerInteractEvent.getPlayer();
                    ItemStack item = playerInteractEvent.getItem();

                    item.setAmount(item.getAmount() - 1);

                    if (playerDataMap.get(player.getName()).isFly())
                        return;

                    playerDataMap.get(player.getName()).setFly(true);
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили доступ к полёту &8(/fly)");
                });

        registerItem("token", objects ->
                        ApiManager
                                .newItemBuilder(Material.MAGMA_CREAM)
                                .setName("§e⛃" + StringUtils.formatDouble(2, (double) objects[0]))
                                .addLore(String.format("§fСодержит - §e⛃%s", StringUtils.replaceComma((double) objects[0])), "§7Нажмите для активации")
                                .build(),
                (playerInteractEvent, itemStack) -> {
                    Player player = playerInteractEvent.getPlayer();
                    double value = Double.parseDouble(StringUtils.unReplaceComma(itemStack.getItemMeta().getLore().get(0).split("⛃")[1]));
                    ItemStack item = playerInteractEvent.getItem();

                    item.setAmount(item.getAmount() - 1);

                    playerDataMap.get(player.getName()).addToken(value);
                    ChatUtil.sendMessage(player,
                            OpPrison.PREFIX + "Вы получили §e⛃%s §8(%s)",
                            StringUtils.formatDouble(2, value),
                            StringUtils.replaceComma(value));
                });

        registerItem("ign",
                ApiManager
                        .newItemBuilder(Material.PAPER)
                        .setName("Чек на 50 рублей §8(ВНУТРИИГРОВЫЕ)")
                        .setLore("§7Нажмите для активации")
                        .build(),
                (playerInteractEvent, itemStack) -> {
                    Action action = playerInteractEvent.getAction();

                    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                        ItemStack item = playerInteractEvent.getItem();

                        item.setAmount(item.getAmount() - 1);

                        MainClass.getInstance().getCorePlayer().getPlayer(playerInteractEvent.getPlayer().getName()).addBalance(50);
                    }
                });

        Arrays.stream(Key.values())
                .forEach(key ->
                        registerItem(
                                String.format("%s_key", key.name().toLowerCase()),
                                objects -> ApiManager.newItemBuilder(key.getStack()).setAmount(((Double) objects[0]).intValue()).build(),
                                (event, itemStack) -> {
                                    Player player = event.getPlayer();
                                    Action action = event.getAction();
                                    Block block = event.getClickedBlock();
                                    if (action == Action.RIGHT_CLICK_BLOCK && event.hasBlock()) {
                                        Case customCase = Case.getCustomCaseByLocation(block);

                                        if (event.getHand() == EquipmentSlot.HAND && customCase != null) {
                                            Key needKey = Key.getKeyFromString(customCase.getKey());

                                            if (needKey == null) {
                                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Ключ не установлен");
                                                return;
                                            }

                                            ItemStack itemKey = needKey.getStack();

                                            if (!key.name().equals(needKey.name())) {
                                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Для открытия этого кейса вам необходим %s", itemKey.getItemMeta().getDisplayName());
                                                return;
                                            }

                                            customCase.open(player);
                                            event.setCancelled(true);
                                        }
                                    }
                                })
                );

        Arrays.stream(GroupsManager.Group.values())
                .forEach(group ->
                        registerItem(
                                String.format("%s_group", group.name().toLowerCase()),
                                objects -> ApiManager.newItemBuilder(group.getStack()).setAmount(((Double) objects[0]).intValue()).build(),
                                (playerInteractEvent, itemStack) -> {
                                    Player player = playerInteractEvent.getPlayer();
                                    ItemStack item = playerInteractEvent.getItem();

                                    item.setAmount(item.getAmount() - 1);

                                    if (new OpPlayer(player).getGroupsManager().isCan(group)) {
                                        return;
                                    }

                                    Achievements achievements = RPlayer.checkAndGet(player.getName()).getAchievements();

                                    achievements.addAchievement(Achievement.OP_ANY_GROUP);


                                    playerDataMap.get(player.getName()).setGroup(group);
                                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили новую группу %s", group.getName());
                                })
                );
    }

    public static void registerItem(String name, Function<Object[], ItemStack> creator, BiConsumer<PlayerInteractEvent, ItemStack> interact) {
        creators.put(name.toLowerCase(), creator);
        if (interact != null)
            interacts.put(name.toLowerCase(), interact);
    }

    public static void registerItem(String name, ItemStack item, BiConsumer<PlayerInteractEvent, ItemStack> interact) {
        registerItem(name, (o) -> item.clone(), interact);
    }

//    protected static void registerItem(String name, ItemStack item) {
//        registerItem(name, item, null);
//    }

    protected static ItemStack setTagName(ItemStack item, String name) {
        return ItemStackUtils.setTag(ApiManager.newItemBuilder(item), "op_item", new NBTTagString(name.toLowerCase())).build();
    }

    public static void interact(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
            String name;

            if ((name = getItemName(event.getItem())).length() > 1) {
                BiConsumer<PlayerInteractEvent, ItemStack> interact = interacts.getOrDefault(name.toLowerCase(), null);
                if (interact == null)
                    return;

                interact.accept(event, event.getItem());
            }
        }
    }

    @AllArgsConstructor
    @Getter
    public enum Key {

        TOKEN("Токен", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§6Токен §fключ").build()),
        MINE("Шахтёрский", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§aШахтёрский §fключ").build()),
        EPIC("Эпический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§5Эпический §fключ").build()),
        LEGENDARY("Легендарный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§eЛегендарный §fключ").build()),
        MYTHICAL("Мифический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§cМифический §fключ").build()),
        SEASON("Сезонный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§bСезонный §fключ").build());

        private String name;
        private ItemStack stack;

        public static Key getKeyFromString(String key) {
            for (Key type : Key.values()) {
                if (type == Key.valueOf(key)) {
                    return type;
                }
            }

            return null;
        }
    }
}