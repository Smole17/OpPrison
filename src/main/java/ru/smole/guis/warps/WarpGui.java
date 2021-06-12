package ru.smole.guis.warps;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.rank.Rank;
import ru.smole.data.rank.RankManager;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.regions.Region;

public class WarpGui extends BaseSimpleInventory {

    private ConfigManager configManager;

    public WarpGui(ConfigManager configManager) {
        super(6, "Точки телепортации (Ранки)");
        this.configManager = configManager;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        RankManager rankManager = opPlayer.getRankManager();

        FileConfiguration config = configManager.getRegionConfig().getConfiguration();
        ConfigurationSection regions = config.getConfigurationSection("regions");

        regions.getKeys(false).forEach(key -> {
            ConfigurationSection section = regions.getConfigurationSection(key);
            String name = section.getString("name");
            int slot = section.getInt("inventory.slot");

            String rankText = section.getString("rank");
            if (rankText == null)
                return;

            Rank needRank = rankManager.getRankFromString(rankText);
            Region region = OpPrison.REGIONS.get(name.toLowerCase());
            Material material = rankManager.isEquals(needRank) ? Material.EMERALD_BLOCK : Material.COAL_BLOCK;

            addItem(slot,
                    ApiManager.newItemBuilder(material)
                            .setName(needRank.getName())
                            .build(),
                    (baseInventory, inventoryClickEvent) -> {
                if (rankManager.isEquals(needRank)) player.teleport(region.getSpawnLocation());
                    });
        });

        for (int i = 1; i < inventory.getSize(); i++) {
            if (i == 46 || i == 47 || i == 49 || i == 50 || i == 51 || i == 53 || i == 54)
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
                        -> new DonateWarpGui(configManager).openInventory(player));

        addItem(52,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .setName("§fШахты для престижей")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui(configManager).openInventory(player));
    }
}
