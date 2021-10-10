package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.achievements.Achievements;

import java.util.*;

public class PickaxeGui extends BaseSimpleInventory {
    public PickaxeGui() {
        super(6, "Прокачка кирки");
    }

    @SuppressWarnings("uncheked")
    @Override
    public void drawInventory(@NonNull Player player) {
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(player.getName());
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        OpPlayer opPlayer = new OpPlayer(player);

        List<String> lore = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            Map<Upgrade, Upgrade.UpgradeStat> upgrades = pickaxe.getUpgrades();
            double pickaxe_level = pickaxe.getLevel();

            int slot = 28 + upgrade.ordinal();

            if (upgrades.get(upgrade) == null) {
                upgrades.put(upgrade, new Upgrade.UpgradeStat(0, true, upgrade.isNeedMessage(), upgrade != Upgrade.JACK_HAMMER));
            }

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
            boolean isCompleteQ = upgrades.get(upgrade).isCompleteQ();

            if (pickaxe_level < need_level || !isCanGroup || !isCompleteQ) {
                ItemUtil.ItemBuilder iBuilder = ApiManager.newItemBuilder(Material.COAL_BLOCK)
                        .setName("§cЗАБЛОКИРОВАНО")
                        .setLore(
                                ""
                        );

                if (upgrade == Upgrade.BLESSINGS)
                    iBuilder = ApiManager.newItemBuilder(upgrade.getMaterial())
                            .setName(upgrade.getName())
                            .setLore("§7" + upgrade.getDescribe(), "");

                if (pickaxe_level <= need_level)
                    iBuilder.addLore(String.format("§c∗ §fДоступно с §b%s §fуровня", StringUtils._fixDouble(0, need_level)));

                if (!isCanGroup)
                    iBuilder.addLore(String.format("§c∗ §fДоступно с %s §fгруппы", group.getName()));

                if (!isCompleteQ)
                    iBuilder.addLore("§c∗ §fНеобходимо прохождение задания");

                if (upgrade == Upgrade.BLESSINGS) {
                    iBuilder.addLore("");
                    iBuilder.addLore(String.format("§a∗ §fСообщения: %s §8(( CTRL + Q ))", isMessage ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));
                }

                addItem(
                        slot,
                        iBuilder.build(),
                        (baseInventory, inventoryClickEvent) -> {
                            if (upgrade == Upgrade.BLESSINGS) {
                                if (inventoryClickEvent.getClick() == ClickType.CONTROL_DROP) {
                                    upgrades.get(upgrade).setMessage(!isMessage);

                                    player.closeInventory();

                                    opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
                                }
                            }
                        }
                );

                continue;
            }

            lore.add("§7" + upgrade.getDescribe());
            lore.add("");

            if (pickaxe_level >= need_level) {
                lore.add(String.format("§fТекущий уровень: §b%s", (int) count));
                lore.add(String.format("§fМаксимальный уровень: §b%s", (int) upgrade.getMax_level()));
                lore.add("");

                if (!upgrade.isMaxLevel(count)) {
                    lore.add(String.format("§a+ §f1 уровень: §e⛃%s §8(( ЛКМ ))", StringUtils.formatDouble(2, needToken)));
                    lore.add(String.format("§a+ §f10 уровней: §e⛃%s §8(( ПКМ ))", StringUtils.formatDouble(2, tenTokens)));
                    lore.add(String.format("§a+ §f%s уровней: §e⛃%s §8(( Q ))",
                            StringUtils.replaceComma(maxUpgrades),
                            // maxUpgrades > upgrade.getMax_level() ? upgrade.getMax_level() : maxUpgrades
                            StringUtils.formatDouble(2, maxTokens)));
                    lore.add("");
                }

                lore.add(String.format("§a∗ §fСтатус: %s §8(( СКМ ))", isIs ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));

                if (upgrade.isNeedMessage())
                    lore.add(String.format("§a∗ §fСообщения: %s §8(( CTRL + Q ))", isMessage ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));
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

            addItem(
                    slot,
                    item,
                    (baseInventory, inventoryClickEvent) -> {
                        ClickType clickType = inventoryClickEvent.getClick();

                        if (upgrade.isNeedMessage()) {
                            if (clickType == ClickType.CONTROL_DROP) {
                                upgrades.get(upgrade).setMessage(!isMessage);

                                player.closeInventory();

                                opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);
                                return;
                            }
                        }

                        if (clickType == ClickType.MIDDLE) {
                            upgrades.get(upgrade).setIs(!isIs);

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
                                if (playerToken >= needToken)
                                    updatePickaxe(playerData, player, needToken, upgrade, upgrades, 1);

                                return;

                            case RIGHT:
                                if (playerToken >= tenTokens) {
                                    double up = 10;

                                    if (count + up > upgrade.getMax_level())
                                        up = maxUpgrades;

                                    updatePickaxe(playerData, player, tenTokens, upgrade, upgrades, up);
                                    return;
                                }

                                return;

                            case DROP:
                                if (playerToken >= maxTokens)
                                    updatePickaxe(playerData, player, maxTokens, upgrade, upgrades, maxUpgrades);

                        }
                    });

            lore.clear();
        }

        addItem(14, Objects.requireNonNull(Items.getItem("pickaxe", player.getName())));
        setGlassPanel();
    }


    protected void updatePickaxe(PlayerData playerData, Player player, double tokens, Upgrade upgrade, Map<Upgrade, Upgrade.UpgradeStat> upgradeMap, double upgrades) {
        playerData.setToken(playerData.getToken() - tokens);
        Upgrade.UpgradeStat up = upgradeMap.get(upgrade);
        up.setCount(up.getCount() + upgrades);

        OpPlayer opPlayer = new OpPlayer(player);
        opPlayer.set(Items.getItem("pickaxe", player.getName()), 1);

        Achievements achievements = RPlayer.checkAndGet(player.getName()).getAchievements();

        switch (upgrade) {
            case BLESSINGS:
                achievements.addAchievement(Achievement.OP_BLESSINGS_LEVEL);

                break;

            case EFFICIENCY:
                if (upgrade.isMaxLevel(up.getCount()))
                    achievements.addAchievement(Achievement.OP_MAX_EFFICIENCY_LEVEL);

                break;

            case FORTUNE:
                if (upgrade.isMaxLevel(up.getCount()))
                    achievements.addAchievement(Achievement.OP_MAX_FORTUNE_LEVEL);

                break;
        }

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
