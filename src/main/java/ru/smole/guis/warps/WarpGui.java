package ru.smole.guis.warps;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.smole.utils.StringUtils;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventory;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.TeleportUtil;

import java.util.Objects;

public class WarpGui extends BaseSimpleInventory {
    private final ConfigManager configManager;

    public WarpGui(ConfigManager configManager) {
        super(5, "Точки телепортации (Основные)");
        this.configManager = configManager;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        FileConfiguration config = configManager.getRegionConfig().getConfiguration();
        ConfigurationSection regions = config.getConfigurationSection("regions");

        regions.getKeys(false).forEach(key -> {
            ConfigurationSection section = regions.getConfigurationSection(key);
            ConfigurationSection items = section.getConfigurationSection("items");

            if (items == null)
                return;

            for (String s : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(s);
                String displayName = item.getString("display-name");
                String[] spawnLoc = item.getString("teleport").split("\\s");

                if (displayName == null)
                    return;

                String material = item.getString("inventory.material");
                int slot = item.getInt("inventory.slot");

                if (material == null)
                    return;


                Location location = new Location(
                        Bukkit.getWorld(spawnLoc[0]),
                        Double.parseDouble(spawnLoc[1]), Double.parseDouble(spawnLoc[2]), Double.parseDouble(spawnLoc[3]),
                        Float.parseFloat(spawnLoc[4]), Float.parseFloat(spawnLoc[5])
                );

                TeleportUtil tp = new TeleportUtil(OpPrison.getInstance());

                addItem(slot,
                        ApiManager.newItemBuilder(Material.valueOf(material.toUpperCase()))
                                .setName(displayName)
                                .setLore("§7Нажмите, для телепортации")
                                .build(),
                        (baseInventory, inventoryClickEvent) -> {
                            double needPrestige = item.getDouble("prestige");

                            if (needPrestige != 0) {
                                if (playerData.getPrestige() >= needPrestige) {
                                    tp.teleport(player, location, "§bТелепортация...", "§7Подождите немного");
                                    player.closeInventory();

                                    player.setFlying(false);
                                    player.setAllowFlight(false);
                                    playerData.setFly(false);
                                }

                                return;
                            }

                            tp.teleport(player, location, "§bТелепортация...", "§7Подождите немного");
                            player.closeInventory();
                        });
            }
        });

        addItem(21,
                ApiManager.newItemBuilder(Material.DIAMOND)
                        .setName("§fШахты для групп")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui(configManager).openInventory(player));

        addItem(25,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .setName("§fШахты престижей")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new PrestigeWarpGui(configManager).openInventory(player));

        setGlassPanel(getInventoryRows(), this);
    }

    public static class PrestigeWarpGui extends BaseSimpleInventory {
        private ConfigManager configManager;

        public PrestigeWarpGui(ConfigManager configManager) {
            super(5, "Точки телепортации (Престиж)");
            this.configManager = configManager;
        }

        @Override
        public void drawInventory(@NonNull Player player) {
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

            FileConfiguration config = configManager.getRegionConfig().getConfiguration();
            ConfigurationSection regions = config.getConfigurationSection("regions");

            regions.getKeys(false).forEach(key -> {
                ConfigurationSection section = regions.getConfigurationSection(key);
                String name = section.getString("name");
                int slot = section.getInt("inventory.slot");
                double needPrestige = section.getDouble("prestige");

                if (section.getDouble("prestige") == 0)
                    return;

                boolean is = playerData.getPrestige() >= needPrestige;
                Region region = OpPrison.REGIONS.get(name.toLowerCase());
                TeleportUtil tp = new TeleportUtil(OpPrison.getInstance());
                Material material = is ? Material.EMERALD_BLOCK : Material.COAL_BLOCK;

                addItem(slot,
                        ApiManager.newItemBuilder(material)
                                .setName("§a" + name + " §fпрестижей шахта")
                                .setLore(is ? "§7Нажмите, для телепортации" : null)
                                .build(),
                        (baseInventory, inventoryClickEvent) -> {
                            if (is) {
                                tp.teleport(player, region.getSpawnLocation(), "§bТелепортация...", "§7Подождите немного");
                                player.closeInventory();
                            }
                        });
            });

            addItem(42,
                    ApiManager.newItemBuilder(Material.DIAMOND)
                            .setName("§aШахты для групп")
                            .build(), (baseInventory, inventoryClickEvent)
                            -> new DonateWarpGui(configManager).openInventory(player));

            addItem(40,
                    ApiManager.newItemBuilder(Material.BOOK)
                            .setName("§aОсновные локации")
                            .build(), (baseInventory, inventoryClickEvent)
                            -> new WarpGui(configManager).openInventory(player));

            setGlassPanel(getInventoryRows(), this);
        }
    }

    public static class DonateWarpGui extends BaseSimpleInventory {

        private final ConfigManager configManager;

        public DonateWarpGui(ConfigManager configManager) {
            super(5, "Точки телепортации (Группы)");
            this.configManager = configManager;
        }

        @Override
        public void drawInventory(@NonNull Player player) {
            FileConfiguration config = configManager.getRegionConfig().getConfiguration();
            ConfigurationSection regions = config.getConfigurationSection("regions");

            regions.getKeys(false).forEach(key -> {
                ConfigurationSection section = regions.getConfigurationSection(key);
                String name = section.getString("name");
                int slot = section.getInt("inventory.slot");
                String group = section.getString("group");

                if (group == null)
                    return;

                boolean is = new OpPlayer(player)
                        .getGroupsManager()
                        .isCan(Objects.requireNonNull(GroupsManager.Group.getGroupFromString(group.toUpperCase())));

                Region region = OpPrison.REGIONS.get(name.toLowerCase());
                TeleportUtil tp = new TeleportUtil(OpPrison.getInstance());
                Material material = is ? Material.EMERALD_BLOCK : Material.COAL_BLOCK;

                addItem(slot,
                        ApiManager.newItemBuilder(material)
                                .setName(name + " §fшахта")
                                .setLore(is ? "§7Нажмите, для телепортации" : null)
                                .build(),
                        (baseInventory, inventoryClickEvent) -> {
                            if (is) {
                                tp.teleport(player, region.getSpawnLocation(), "§bТелепортация...", "§7Подождите немного");
                                player.closeInventory();
                            }
                        });
            });

            addItem(40,
                    ApiManager.newItemBuilder(Material.BOOK)
                            .setName("§fОсновные локации")
                            .build(), (baseInventory, inventoryClickEvent)
                            -> new WarpGui(configManager).openInventory(player));

            addItem(42,
                    ApiManager.newItemBuilder(Material.NETHER_STAR)
                            .setName("§fШахты престижей")
                            .build(), (baseInventory, inventoryClickEvent)
                            -> new PrestigeWarpGui(configManager).openInventory(player));

            setGlassPanel(getInventoryRows(), this);
        }
    }

    private static void setGlassPanel(int rows, BaseSimpleInventory inv) {
        for (int i = 1; i <= rows * 9; i++) {
            BaseInventoryItem item = inv.getInventoryInfo().getItem(i - 2);

            if (item == null)
                inv.addItem(
                        i,
                        ApiManager
                                .newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build()
                );
        }
    }
}
