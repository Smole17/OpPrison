package ru.smole.data.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.NBTTagString;
import net.minecraft.server.v1_12_R1.PlayerAbilities;
import org.apache.logging.log4j.util.BiConsumer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.luvas.rmcs.MainClass;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.cases.Case;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.guis.MenuGui;
import ru.smole.guis.PickaxeGui;
import ru.smole.guis.warps.WarpGui;
import ru.smole.utils.ItemStackUtils;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.player.CorePlayer;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.ItemUtil;
import ru.xfenilafs.core.util.NumberUtil;
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
                                .setName("§fДоступ к полёту §8(/fly)")
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
                        .setLore("§7Нажмите пкм и напишите в тикет дискорд сервера",
                                "§7(приложите полный скриншот инвентаря с предметом со временем)"
                        )
                        .build(),
                (playerInteractEvent, itemStack) -> ChatUtil.sendMessage(playerInteractEvent.getPlayer(), OpPrison.PREFIX + "https://discord.io/starfarm"));

        Arrays.stream(Key.values())
                .forEach(key ->
                        registerItem(
                                String.format("%s_key", key.name().toLowerCase()),
                                objects -> ApiManager.newItemBuilder(key.getStack()).setAmount(((Double) objects[0]).intValue()).build(),
                                (event, itemStack) -> {
                                    Player player = event.getPlayer();
                                    Block block = event.getClickedBlock();
                                    Action action = event.getAction();
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

        registerItem("sponge",
                objects -> ApiManager
                        .newItemBuilder(Material.SPONGE)
                        .setName("§e" + StringUtils._fixDouble(0, (Double) objects[0]) + " §fОчков")
                        .setLore(
                                "§7Нажмите §8ШИФТ+ПКМ в руке §7для использования!",
                                "§7Добавляет " + StringUtils._fixDouble(0, (Double) objects[0]) + " очков в банду")
                        .build(),
                (event, itemStack) -> {
                    Player player = event.getPlayer();
                    String playerName = player.getName();

                    GangDataManager gangDataManager = OpPrison.getInstance().getGangDataManager();
                    Action action = event.getAction();

                    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
                        return;

                    if (!player.isSneaking())
                        return;

                    if (!gangDataManager.playerHasGang(playerName)) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "У вас нет банды");
                        return;
                    }

                    double count = Double.parseDouble(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()).split(" ")[0]);

                    GangData gangData = gangDataManager.getGangFromPlayer(playerName);

                    gangData.getGangPlayerMap().get(playerName.toLowerCase()).addScore(count);
                    gangData.addScore(count);
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + StringUtils._fixDouble(0, count) + " очков");
                });

        Arrays.stream(Material.values())
                .filter(material ->
                        material == Material.CHAINMAIL_BOOTS || material == Material.CHAINMAIL_LEGGINGS ||
                                material == Material.CHAINMAIL_CHESTPLATE || material == Material.CHAINMAIL_HELMET ||
                        material == Material.IRON_BOOTS || material == Material.IRON_LEGGINGS
                                || material == Material.IRON_CHESTPLATE || material == Material.IRON_HELMET
                                || material == Material.DIAMOND_BOOTS || material == Material.DIAMOND_LEGGINGS
                                || material == Material.DIAMOND_CHESTPLATE || material == Material.DIAMOND_HELMET
                )
                .forEach(material ->
                        registerItem(material.name().toLowerCase(),
                                objects -> {
                                    ItemUtil.ItemBuilder itemBuilder = ApiManager
                                            .newItemBuilder(material)
                                            .setName(
                                                    (material.name().contains("BOOTS") ? "§fБотинки" :
                                                            material.name().contains("LEGGINGS") ? "§fПоножи" :
                                                                    material.name().contains("CHESTPLATE") ? "§fНагрудник" : "§fШлем")
                                                            + (material.name().contains("IRON") ? " §8(§7ЖЕЛЕЗНАЯ§8)" :
                                                            material.name().contains("CHAINMAIL") ? " §8(§8КОЛЬЧУЖНАЯ§8)" : " §8(§bАЛМАЗНАЯ§8)")
                                            );

                                    int protection = ((Double) objects[0]).intValue();

                                    if (protection != 0)
                                        itemBuilder.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection);

                                    return itemBuilder.build();
                                })
                );

        Arrays.stream(Material.values())
                .filter(material ->
                        material == Material.IRON_SWORD || material == Material.DIAMOND_SWORD
                )
                .forEach(material ->
                        registerItem(material.name().toLowerCase(),
                                objects -> {
                                    ItemUtil.ItemBuilder itemBuilder = ApiManager
                                            .newItemBuilder(material)
                                            .setName(
                                                    "§fМеч" + (material.name().contains("IRON") ? " §8(§7ЖЕЛЕЗНЫЙ§8)" : " §8(§bАЛМАЗНЫЙ§8)")
                                            );

                                    int damageAll = ((Double) objects[0]).intValue();

                                    if (damageAll != 0)
                                        itemBuilder.addEnchantment(Enchantment.DAMAGE_ALL, damageAll);

                                    return itemBuilder.build();
                                })
                );

        registerItem("location_gui",
                objects -> ApiManager
                        .newItemBuilder(Material.NETHER_STAR)
                        .setName("§fМеню Режима")
                        .addItemFlags(
                                ItemFlag.HIDE_ATTRIBUTES,
                                ItemFlag.HIDE_DESTROYS,
                                ItemFlag.HIDE_UNBREAKABLE
                        )
                        .setLore("§7Нажмите для использования!")
                        .build(),
                (event, itemStack) -> new MenuGui().openInventory(event.getPlayer()));

        registerItem(Material.FISHING_ROD.name().toLowerCase(),
                objects -> ApiManager
                        .newItemBuilder(Material.FISHING_ROD)
                        .setName("§fУдочка")
                        .build());

        registerItem("bow",
                objects -> ApiManager
                        .newItemBuilder(Material.BOW)
                        .setName("§fЛук")
                        .addEnchantment(Enchantment.ARROW_DAMAGE, ((Double) objects[0]).intValue())
                        .build()
        );

        registerItem("arrow", objects -> ApiManager.newItemBuilder(Material.ARROW).setAmount(((Double) objects[0]).intValue()).build());

        registerItem("ender_pearl", objects -> ApiManager.newItemBuilder(Material.ENDER_PEARL).setAmount(((Double) objects[0]).intValue()).build());

        registerItem("speed_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.POTION)
                        .setName("§fЗелье Скорости " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 150, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.SPEED.getColor())
                        .build()
        );

        registerItem("jump_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.POTION)
                        .setName("§fЗелье §aПрыгучести " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 150, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.JUMP.getColor())
                        .build()
        );

        registerItem("regen_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.POTION)
                        .setName("§fЗелье §dРегенерации " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.REGENERATION.getColor())
                        .build()
        );

        registerItem("str_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.POTION)
                        .setName("§fЗелье §4Силы " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.INCREASE_DAMAGE.getColor())
                        .build()
        );

        registerItem("heal_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.SPLASH_POTION)
                        .setName("§fЗелье §cМгновенного Лечения " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.HEAL, 0, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.HEAL.getColor())
                        .build()
        );

        registerItem("slow_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.SPLASH_POTION)
                        .setName("§fЗелье §9Медлительности " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.SLOW.getColor())
                        .build()
        );

        registerItem("poison_potion",
                objects -> ApiManager
                        .newItemBuilder(Material.SPLASH_POTION)
                        .setName("§fЗелье §2Отравления " + (((Double) objects[0]).intValue() + 1))
                        .addCustomPotionEffect(new PotionEffect(PotionEffectType.POISON , 20 * 7, ((Double) objects[0]).intValue()), false)
                        .setPotionColor(PotionEffectType.SLOW.getColor())
                        .build()
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

    public static void registerItem(String name, Function<Object[], ItemStack> creator) {
        registerItem(name, creator, null);
    }

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

        TOKEN("Токен", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§6Токен Ключ").build()),
        MINE("Шахтёрский", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§aШахтёрский Ключ").build()),
        EPIC("Эпический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§5Эпический Ключ").build()),
        LEGENDARY("Легендарный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§eЛегендарный Ключ").build()),
        MYTHICAL("Мифический", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§cМифический Ключ").build()),
        SEASON("Сезонный", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§bСезонный Ключ").build()),
        GROUP("Групповой", ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK).setName("§dГрупповой Ключ").build());

        private final String name;
        private final ItemStack stack;

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