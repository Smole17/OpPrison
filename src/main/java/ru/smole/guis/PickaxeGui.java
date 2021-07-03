package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PickaxeGui extends BaseSimpleInventory {
    public PickaxeGui() {
        super(6, "Прокачка кирки");
    }

    @SuppressWarnings("uncheked")
    @Override
    public void drawInventory(@NonNull Player player) {
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(player.getName());
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        List<String> lore = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            Map<Upgrade, Double> upgrades = pickaxe.getUpgrades();
            double count = upgrades.get(upgrade);
            Material type = upgrade.getMaterial();

            double needToken = upgrade.getNeedTokens(count);
            double needToken10 = upgrade.getNeedTokens(count + 10);

            lore.add("describe");
            lore.add("");
            lore.add(String.format("§b§l* §fТекущий уровень: §b%s", (int) count));
            lore.add(String.format("§b§l* §fМаксимальный уровень: §b%s", (int) upgrade.getMax_level()));
            lore.add("");

            double maxUpgrades = (double) upgrade.getMaxUpgrades(playerData, count, -1)[0];
            double maxTokens = (double) upgrade.getMaxUpgrades(playerData, count, -1)[1];

            if (!upgrade.isMaxLevel(count)) {
                lore.add(String.format("+ §f1 уровень: §e%s §8(( ЛКМ ))", StringUtils.formatDouble(2, needToken)));
                lore.add(String.format("+ §f10 уровней: §e%s §8(( ПКМ ))", StringUtils.formatDouble(2, needToken10)));
                lore.add(String.format("+ §f%s уровней: §e%s §8(( Q ))",
                        StringUtils.formatDouble(2, maxUpgrades > upgrade.getMax_level() ? upgrade.getMax_level() : maxUpgrades),
                        StringUtils.formatDouble(2, maxTokens)));
            }

            ItemUtil.ItemBuilder itemBuilder =
                    ApiManager.newItemBuilder(type)
                            .setName(upgrade.getName())
                            .setLore(lore);

            if (type == Material.DOUBLE_PLANT)
                itemBuilder.setDurability(0);

            ItemStack item = itemBuilder.build();
            int slot = 27 + upgrade.ordinal();

            addItem(
                    slot,
                    item,
                    (baseInventory, inventoryClickEvent) -> {
                        if (upgrade.isMaxLevel(count))
                            return;

                        ClickType clickType = inventoryClickEvent.getClick();
                        double playerToken = playerData.getToken();

                        if (clickType.isLeftClick()) {
                            if (playerToken >= needToken) {
                                upgrades.remove(upgrade);
                                upgrades.put(upgrade, count +1);
                            }

                            return;
                        }

                        if (clickType.isRightClick()) {
                            double maxUpgrades10 = (double) upgrade.getMaxUpgrades(playerData, count, 10)[0];
                            List<Double> tokens = (List<Double>) upgrade.getMaxUpgrades(playerData, count, 10)[2];

                            tokens.forEach((token) -> {
                                if (playerToken >= token) {
                                    upgrades.remove(upgrade);
                                    upgrades.put(upgrade, count + maxUpgrades10);
                                }
                            });

                            return;
                        }
                        
                        if (clickType.isKeyboardClick()) {
                            if (playerToken >= maxTokens) {
                                upgrades.remove(upgrade);
                                upgrades.put(upgrade, maxUpgrades);
                            }
                        }
                    });

            lore.clear();
        }
    }
}
