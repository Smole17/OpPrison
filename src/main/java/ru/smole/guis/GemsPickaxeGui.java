package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import ru.smole.data.group.GroupsManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventory;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ItemUtil;
import sexy.kostya.mineos.achievements.Achievement;
import sexy.kostya.mineos.achievements.Achievements;

import java.util.*;

public class GemsPickaxeGui extends BaseSimpleInventory {
    public GemsPickaxeGui() {
        super(6, "§7Престижные Зачарования Кирки");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        Pickaxe pickaxe = PickaxeManager.getPickaxes().get(player.getName());
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

        List<String> lore = new ArrayList<>();
        int i = 0;
        for (Upgrade upgrade : Upgrade.values()) {
            if (upgrade.getType() == Upgrade.UpgradeType.TOKEN)
                continue;

            Map<Upgrade, Upgrade.UpgradeStat> upgrades = pickaxe.getUpgrades();
            double pickaxe_level = pickaxe.getLevel();

            if ((30 + i + 1) % 9 == 0)
                i = i + 4;

            int slot = 30 + i;
            i = i + 1;

            if (upgrades.get(upgrade) == null) {
                upgrades.put(upgrade, new Upgrade.UpgradeStat(0, true, upgrade.isNeedMessage()));
            }

            double count = upgrades.get(upgrade).getCount();
            Material type = upgrade.getMaterial();
            double need_level = upgrade.getNeed_level_pickaxe();

            double needToken = upgrade.getNeedTokens(count + 1);
            double tenTokens = (double) upgrade.get10Upgrades(count + 1)[1];

            double maxUpgrades = (double) upgrade.getMaxUpgrades(count + 1, playerData.getGems())[0];
            double maxTokens = (double) upgrade.getMaxUpgrades(count + 1, playerData.getGems())[1];

            GroupsManager.Group group = upgrade.getGroup();
            boolean isCanGroup = group.isCan(playerData.getGroup());
            boolean isIs = upgrades.get(upgrade).isIs();
            boolean isMessage = upgrades.get(upgrade).isMessage();

            if (pickaxe_level < need_level || !isCanGroup) {
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

                                    Arrays.stream(player.getInventory().getStorageContents())
                                            .parallel()
                                            .filter(itemStack1 -> itemStack1 != null && itemStack1.getType() == Material.DIAMOND_PICKAXE)
                                            .forEach(itemStack1 -> itemStack1.setAmount(0));

                                    OpPlayer.add(player, Items.getItem("pickaxe", player.getName()));
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
                    lore.add(String.format("§a+ §f1 уровень: §3❅%s §8(( ЛКМ ))", StringUtils.formatDouble(2, needToken)));
                    lore.add(String.format("§a+ §f10 уровней: §3❅%s §8(( ПКМ ))", StringUtils.formatDouble(2, tenTokens)));
                    lore.add(String.format("§a+ §f%s уровней: §3❅%s §8(( Q ))",
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

                                Arrays.stream(player.getInventory().getStorageContents())
                                        .parallel()
                                        .filter(itemStack1 -> itemStack1 != null && itemStack1.getType() == Material.DIAMOND_PICKAXE)
                                        .forEach(itemStack1 -> itemStack1.setAmount(0));

                                OpPlayer.add(player, Items.getItem("pickaxe", player.getName()));
                                return;
                            }
                        }

                        if (clickType == ClickType.MIDDLE) {
                            upgrades.get(upgrade).setIs(!isIs);

                            player.closeInventory();

                            Arrays.stream(player.getInventory().getStorageContents())
                                    .parallel()
                                    .filter(itemStack1 -> itemStack1 != null && itemStack1.getType() == Material.DIAMOND_PICKAXE)
                                    .forEach(itemStack1 -> itemStack1.setAmount(0));

                            OpPlayer.add(player, Items.getItem("pickaxe", player.getName()));

                            return;
                        }

                        if (upgrade.isMaxLevel(count)) {
                            player.closeInventory();
                            return;
                        }

                        double playerGems = playerData.getGems();

                        switch (clickType) {
                            case LEFT:
                                if (playerGems >= needToken)
                                    updatePickaxe(playerData, player, needToken, upgrade, upgrades, 1);

                                return;

                            case RIGHT:
                                if (playerGems >= tenTokens) {
                                    double up = 10;

                                    if (count + up > upgrade.getMax_level())
                                        up = maxUpgrades;

                                    updatePickaxe(playerData, player, tenTokens, upgrade, upgrades, up);
                                    return;
                                }

                                return;

                            case DROP:
                                if (playerGems >= maxTokens)
                                    updatePickaxe(playerData, player, maxTokens, upgrade, upgrades, maxUpgrades);

                        }
                    });

            lore.clear();
        }

        addItem(14, Objects.requireNonNull(Items.getItem("pickaxe", player.getName())));

        addItem(50,
                ApiManager.newItemBuilder(Material.ARROW)
                        .setName("§cНазад")
                        .build(),
                (baseInventory, inventoryClickEvent) -> new PickaxeGui().openInventory(player));

        setGlassPanel();
    }

    protected void updatePickaxe(PlayerData playerData, Player player, double gems, Upgrade upgrade, Map<Upgrade, Upgrade.UpgradeStat> upgradeMap, double upgrades) {
        playerData.setGems(playerData.getGems() - gems);
        Upgrade.UpgradeStat up = upgradeMap.get(upgrade);
        up.setCount(up.getCount() + upgrades);

        Arrays.stream(player.getInventory().getStorageContents())
                .parallel()
                .filter(itemStack1 -> itemStack1 != null && itemStack1.getType() == Material.DIAMOND_PICKAXE)
                .forEach(itemStack1 -> itemStack1.setAmount(0));

        OpPlayer.add(player, Items.getItem("pickaxe", player.getName()));

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
        for (int i = 1; i <= this.getInventoryRows() * 9; i++) {
            BaseInventoryItem item = this.getInventoryInfo().getItem(i - 2);

            if (item == null)
                this.addItem(
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
