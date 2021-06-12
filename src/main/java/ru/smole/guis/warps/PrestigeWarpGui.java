package ru.smole.guis.warps;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.player.OpPlayer;
import ru.smole.rank.Rank;
import ru.smole.rank.RankManager;
import ru.smole.utils.StringUtils;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryClickHandler;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.regions.Region;

import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

public class PrestigeWarpGui extends BaseSimpleInventory {

    private ConfigManager configManager;

    public PrestigeWarpGui(ConfigManager configManager) {
        super(6, "Точки телепортации (Престиж)");
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
            if (section.getConfigurationSection("prestige") == null)
                return;

            boolean is = playerData.getPrestige() >= needPrestige;
            Region region = OpPrison.REGIONS.get(name.toLowerCase());
            Material material = is ? Material.EMERALD_BLOCK : Material.COAL_BLOCK;

            addItem(slot,
                    ApiManager.newItemBuilder(material)
                            .setName("§f" + StringUtils.formatDouble(0, needPrestige) + " престижей")
                            .build(),
                    (baseInventory, inventoryClickEvent) -> {
                        if (is) player.teleport(region.getSpawnLocation());
                    });
        });

        for (int i = 1; i <= inventory.getSize(); i++) {
            if (i == 46 || i == 47 || i == 49 || i == 51 || i == 52 || i == 53 || i == 54)
                addItem(i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build());
        }

        addItem(48,
                ApiManager.newItemBuilder(Material.DIAMOND)
                        .setName("§fШахты для привилегий")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui(configManager).drawInventory(player));

        addItem(50,
                ApiManager.newItemBuilder(Material.IRON_INGOT)
                        .setName("§fШахты для ранков")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new WarpGui(configManager).drawInventory(player));

        addHandler(BaseInventoryClickHandler.class, (baseInventory, inventoryClickEvent) -> inventoryClickEvent.setCancelled(true));
    }
}
