package ru.smole.commands;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;

public class KitCommand extends BukkitCommand<Player> {
    public KitCommand() {
        super("kit", "gkit", "kits");
    }

    @Override
    protected void onExecute(Player player, String[] strings) {
        new KitsGui(OpPrison.getInstance().getConfig().getConfigurationSection("kits")).openInventory(player);
    }

    public static class KitsGui extends BaseSimpleInventory {

        private ConfigurationSection section;
        private static Map<String, List<String>> playerKits = new HashMap<>();

        public KitsGui(ConfigurationSection section) {
            super(5, "Наборы");
            this.section = section;
        }

        @Override
        public void drawInventory(@NonNull Player player) {
            setCategory(player);
        }

        private void setCategory(Player player) {
            clearInventory();

            addItem(10,
                    ApiManager.newItemBuilder(Material.ENDER_PEARL)
                            .setName("§bНаборы групп --->")
                            .setLore("§7Выберите нужный набор")
                            .build()
            );

            addItem(28,
                    ApiManager.newItemBuilder(Material.EYE_OF_ENDER)
                            .setName("§bГлобальные наборы --->")
                            .setLore("§7Выберите нужный набор")
                            .build()
            );

            setKits(player);
            setGlassPanel();
        }

        private void setKits(Player player) {
            int i = 11;
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

            for (GroupsManager.Group group : GroupsManager.Group.values()) {
                if (group == GroupsManager.Group.MANTLE || group == GroupsManager.Group.ADMIN)
                    continue;

                String kit = group.name().toLowerCase();
                List<ItemStack> items = getItems(section, kit);

                if (items == null)
                    continue;

                addItem(i,
                        ApiManager
                                .newItemBuilder(Material.BOOK)
                                .setName(group.getName() + " §fнабор")
                                .setLore("§7Нажмите ЛКМ, чтобы забрать набор", "§7Нажмите ПКМ, чтобы просмотреть список предметво")
                                .build(),
                        (baseInventory, inventoryClickEvent) -> {
                            ClickType clickType = inventoryClickEvent.getClick();

                            if (clickType == ClickType.RIGHT) {
                                new KitsContentsGui(items, this).openInventory(player);
                                return;
                            }

                            if (!group.isCan(playerData.getGroup())) {
                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "У вас нет доступа к данному набору");
                                return;
                            }

                            click(player, clickType, kit, group.getName(), items);
                        }
                );

                i++;
            }

            ConfigurationSection globalSec = section.getConfigurationSection("global");

            int f = 29;
            for (String kit : globalSec.getKeys(false)) {

                String name = globalSec.getString(kit + ".display-name");
                String material = globalSec.getString(kit + ".material").toUpperCase();
                List<ItemStack> items = getItems(globalSec, kit);

                addItem(f,
                        ApiManager
                                .newItemBuilder(Material.valueOf(material))
                                .setName(name)
                                .setLore("§7Нажмите ЛКМ, чтобы забрать набор", "§7Нажмите ПКМ, чтобы просмотреть список предметов")
                                .build(),
                        (baseInventory, inventoryClickEvent) -> {
                            ClickType clickType = inventoryClickEvent.getClick();

                            if (clickType == ClickType.RIGHT) {
                                new KitsContentsGui(items, this).openInventory(player);
                                return;
                            }

                            if (!playerData.getAccess().contains(kit)) {
                                ChatUtil.sendMessage(player, OpPrison.PREFIX + "У вас нет доступа к данному набору");
                                return;
                            }

                            click(player, clickType, kit, name, items);
                        });

                f++;
            }
        }

        private void setGlassPanel() {
            for (int i = 1; i <= getInventoryRows() * 9; i++) {
                BaseInventoryItem item = getInventoryInfo().getItem(i - 2);

                if (item == null)
                    addItem(
                            i,
                            ApiManager
                                    .newItemBuilder(Material.STAINED_GLASS_PANE)
                                    .setName(" ")
                                    .setDurability(7)
                                    .build()
                    );
            }
        }

        protected void click(Player player, ClickType clickType, String kit, String name, List<ItemStack> items) {
            OpPlayer opPlayer = new OpPlayer(player);
            String playerName = player.getName();

            if (clickType == ClickType.LEFT) {
                if (!playerKits.isEmpty())
                    if (playerKits.containsKey(playerName) && playerKits.get(playerName).contains(kit)) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы уже забирали данный набор");
                        return;
                    }

                try {
                    GroupsManager.Group group = GroupsManager.Group.getGroupFromString(kit.toUpperCase());

                    if (group != null)
                        if (!group.isCan(OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName).getGroup())) {
                            ChatUtil.sendMessage(player, OpPrison.PREFIX + "У вас нет доступа к набору");
                            return;
                        }
                } catch (Exception ignored) {}


                List<String> kitsList = playerKits.get(playerName);
                kitsList.add(kit);
                playerKits.remove(playerName);
                playerKits.put(playerName, kitsList);

                items.forEach(itemStack -> {
                    opPlayer.add(itemStack);
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы получили %s &fс набора %s", itemStack.getItemMeta().getDisplayName(), name);
                });
            }
        }

        public List<ItemStack> getItems(ConfigurationSection section, String scName) {
            scName = scName.toLowerCase();
            ConfigurationSection sc = section.getConfigurationSection(scName);

            if (sc == null)
                return null;

            ConfigurationSection itemsSec = sc.getConfigurationSection("items");

            if (itemsSec == null)
                return null;

            List<ItemStack> items = new ArrayList<>();

            for (String key : itemsSec.getKeys(false)) {
                items.add(Items.getItem(itemsSec.getString(key + ".name"), itemsSec.getDouble(key + ".value")));
            }

            return items;
        }

        public static void load(String playerName, String kitsSQL) {
            playerKits.put(playerName, new ArrayList<>());

            List<String> kitsList = playerKits.get(playerName);
            kitsList.addAll(Arrays.asList(kitsSQL.split(",")));
        }

        public static String save(String playerName) {
            if (playerKits.isEmpty())
                return null;

            if (!playerKits.containsKey(playerName))
                    return null;

            if (playerKits.get(playerName).isEmpty())
                return null;

            StringBuilder builder = new StringBuilder();
            String format = "%s,";

            int i = 0;

            for (String s : playerKits.get(playerName)) {
                if (i == playerKits.get(playerName).size() - 1) {
                    format = format.replace(",", "");
                }

                builder.append(String.format(format, s));
                i += 1;
            }

            List<String> kitsList = playerKits.get(playerName);
            kitsList.clear();
            playerKits.remove(playerName);

            return builder.toString();
        }


        protected static class KitsContentsGui extends BaseSimpleInventory {
            private List<ItemStack> items;
            private BaseSimpleInventory inv;

            public KitsContentsGui(List<ItemStack> items, BaseSimpleInventory inv) {
                super(1, "Содержимое набора");
                this.items = items;
                this.inv = inv;
            }

            @Override
            public void drawInventory(@NonNull Player player) {
                int i = 1;
                for (ItemStack item : items) {
                    addItem(i, item);

                    i += 1;
                }

                addItem(8,
                        ApiManager
                                .newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build());

                addItem(9,
                        ApiManager
                                .newItemBuilder(Material.BARRIER)
                                .setName("§cНазад")
                                .setLore("§7Нажмите, чтобы вернуться назад")
                                .setDurability(7)
                                .build(),
                        (baseInventory, inventoryClickEvent) -> inv.openInventory(player));
            }
        }
    }
}
