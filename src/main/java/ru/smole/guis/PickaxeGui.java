package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;

import java.util.ArrayList;
import java.util.Iterator;
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
            Map<Upgrade, Upgrade.UpgradeStat> upgrades = pickaxe.getUpgrades();
            double pickaxe_level = pickaxe.getLevel();

            double count = upgrades.get(upgrade).getCount();
            Material type = upgrade.getMaterial();
            double need_level = upgrade.getNeed_level_pickaxe();

            double needToken = upgrade.getNeedTokens(count + 1);
            double tenTokens = (double) upgrade.get10Upgrades(count + 1)[1];

            double maxUpgrades = (double) upgrade.getMaxUpgrades(playerData, count + 1)[0];
            double maxTokens = (double) upgrade.getMaxUpgrades(playerData, count + 1)[1];

            GroupsManager.Group group = upgrade.getGroup();
            boolean isCanGroup = group.isCan(playerData.getGroup());
            boolean isIs = upgrades.get(upgrade).isIs();
            boolean isMessage = upgrades.get(upgrade).isMessage();

            lore.add("§8§o" + upgrade.getDescribe());
            lore.add("");

            if (pickaxe_level >= need_level && isCanGroup) {
                lore.add(String.format("§b§l* §fТекущий уровень: §b%s", (int) count));
                lore.add(String.format("§b§l* §fМаксимальный уровень: §b%s", (int) upgrade.getMax_level()));
                lore.add("");

                if (!upgrade.isMaxLevel(count)) {
                    lore.add(String.format("§b§l+ §f1 уровень: §e%s §8(( ЛКМ ))", StringUtils.formatDouble(2, needToken)));
                    lore.add(String.format("§b§l+ §f10 уровней: §e%s §8(( ПКМ ))", StringUtils.formatDouble(2, tenTokens)));
                    lore.add(String.format("§b§l+ §f%s уровней: §e%s §8(( Q ))",
                            StringUtils.formatDouble(0, maxUpgrades),
                            // maxUpgrades > upgrade.getMax_level() ? upgrade.getMax_level() : maxUpgrades
                            StringUtils.formatDouble(2, maxTokens)));
                    lore.add("");
                }

                lore.add(String.format("§b§l* §fСтатус: %s §8(( СКМ ))", isIs ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));
                lore.add(String.format("§b§l* §fСообщения: %s §8(( CTRL + Q ))", isMessage ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));
            } else {
                lore.add(String.format("§b§l* §fДоступно с §b%s §fуровня", StringUtils._fixDouble(0, need_level)));
            }

            if (!isCanGroup) {
                lore.add(String.format("§b§l* §fДоступно от §b%s §fгруппы", group.getName()));
            }

            ItemUtil.ItemBuilder itemBuilder =
                    ApiManager.newItemBuilder(type)
                            .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .addItemFlag(ItemFlag.HIDE_DESTROYS)
                            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                            .setName(upgrade.getName())
                            .setLore(lore);

            if (type == Material.DOUBLE_PLANT)
                itemBuilder.setDurability(0);

            ItemStack item = itemBuilder.build();
            int slot = 28 + upgrade.ordinal();
            OpPlayer opPlayer = new OpPlayer(player);

            addItem(
                    slot,
                    item,
                    (baseInventory, inventoryClickEvent) -> {
                        if (!upgrade.isUnlock(pickaxe_level)) {
                            player.closeInventory();
                            return;
                        }

                        if (!isCanGroup) {
                            player.closeInventory();
                            return;
                        }

                        ClickType clickType = inventoryClickEvent.getClick();

                        if (clickType == ClickType.MIDDLE) {
                            upgrades.remove(upgrade);
                            upgrades.put(upgrade, new Upgrade.UpgradeStat(count, !isIs, isMessage));

                            player.closeInventory();

                            opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
                            return;
                        }

                        if (clickType == ClickType.CONTROL_DROP) {
                            upgrades.remove(upgrade);
                            upgrades.put(upgrade, new Upgrade.UpgradeStat(count, isIs, !isMessage));

                            player.closeInventory();

                            opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
                            return;
                        }

                        if (upgrade.isMaxLevel(count)) {
                            player.closeInventory();
                            return;
                        }

                        double playerToken = playerData.getToken();

                        switch (clickType) {
                            case LEFT:
                                if (playerToken >= needToken) {
                                    double up = count + 1;

                                    updatePickaxe(playerData, player, needToken, new Upgrade.UpgradeStat(up, isIs, isMessage), upgrade, upgrades);
                                    return;
                                }

                                return;

                            case RIGHT:
                                if (playerToken >= tenTokens) {
                                    double up = count + 10;

                                    if (up > upgrade.getMax_level())
                                        return;

                                    updatePickaxe(playerData, player, tenTokens, new Upgrade.UpgradeStat(up, isIs, isMessage), upgrade, upgrades);
                                    return;
                                }

                                return;

                            case DROP:
                                if (playerToken >= maxTokens) {
                                    double up = count + maxUpgrades;

                                    updatePickaxe(playerData, player, maxTokens, new Upgrade.UpgradeStat(up, isIs, isMessage), upgrade, upgrades);
                                }

                        }
                    });

            lore.clear();
        }

        addItem(14, Items.getItem("pickaxe", player.getName()));
        setGlassPanel();
    }


    protected void updatePickaxe(PlayerData playerData, Player player, double tokens, Upgrade.UpgradeStat up, Upgrade upgrade, Map<Upgrade, Upgrade.UpgradeStat> upgradeMap) {
        playerData.setToken(playerData.getToken() - tokens);
        upgradeMap.remove(upgrade);
        upgradeMap.put(upgrade, up);

        OpPlayer opPlayer = new OpPlayer(player);
        opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);

        updateInventory(player);
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
}
