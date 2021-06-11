package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.guis.warps.DonateWarpGui;
import ru.smole.player.OpPlayer;
import ru.smole.rank.Rank;
import ru.smole.rank.RankManager;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventory;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryClickHandler;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryDisplayableHandler;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.regions.Region;

public class WarpGui extends BaseSimpleInventory {

    private ConfigManager configManager;

    public WarpGui(ConfigManager configManager) {
        super(6, "Точки телепортации");
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
            if (i == 45 || i == 46 || i == 48 || i == 49 || i == 50 || i == 52 || i == 53)
                addItem(i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build());
        }

        addItem(47,
                ApiManager.newItemBuilder(Material.DIAMOND)
                        .setName("xz")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui().openInventory(player));

        addItem(51,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .setName("test")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new DonateWarpGui().openInventory(player));
    }
}
