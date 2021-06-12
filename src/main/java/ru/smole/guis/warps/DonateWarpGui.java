package ru.smole.guis.warps;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryClickHandler;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.util.TeleportUtil;

public class DonateWarpGui extends BaseSimpleInventory {

    private ConfigManager configManager;

    public DonateWarpGui(ConfigManager configManager) {
        super(6, "Точки телепортации (Привилегии)");
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

            String permission = section.getString("permission");
            if (permission == null)
                return;

            boolean is = player.hasPermission(permission);
            Region region = OpPrison.REGIONS.get(name.toLowerCase());
            TeleportUtil tp = new TeleportUtil(OpPrison.getInstance());
            Material material = is ? Material.EMERALD_BLOCK : Material.COAL_BLOCK;

            addItem(slot,
                    ApiManager.newItemBuilder(material)
                            .setName(name)
                            .build(),
                    (baseInventory, inventoryClickEvent) -> {
                        if (is) tp.teleport(player, region.getSpawnLocation(), "филя на", "филя дай пожрать пожалуйста");
                    });
        });

        for (int i = 1; i <= inventory.getSize(); i++) {
            if (i == 46 || i == 47 || i == 48 || i == 49 || i == 51 || i == 53|| i == 54)
                addItem(i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build());
        }

        addItem(50,
                ApiManager.newItemBuilder(Material.IRON_INGOT)
                        .setName("§fШахты для ранков")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new WarpGui(configManager).openInventory(player));

        addItem(52,
                ApiManager.newItemBuilder(Material.NETHER_STAR)
                        .setName("§fШахты для престижей")
                        .build(), (baseInventory, inventoryClickEvent)
                        -> new PrestigeWarpGui(configManager).openInventory(player));

        addHandler(BaseInventoryClickHandler.class, (baseInventory, inventoryClickEvent) -> inventoryClickEvent.setCancelled(true));
    }
}
