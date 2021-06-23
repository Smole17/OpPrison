package ru.smole.data.items.pickaxe;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.data.OpPlayer;
import ru.smole.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickaxeManager {

    private @Getter Map<String, Pickaxe> pickaxes;
    private Player player;

    private String name;
    private List<Map<Upgrade, Double>> upgrades;
    private Map<Upgrade, Double> upgradeMap;

    public PickaxeManager(Player player) {
        pickaxes = new HashMap<>();
        this.player = player;
        name = player.getName();
        upgrades = new ArrayList<>();
        upgradeMap = new HashMap<>();
    }

    public void create() {
        OpPlayer opPlayer = new OpPlayer(player);
        for (Upgrade upgrade : Upgrade.values()) {
            upgradeMap.put(upgrade, upgrade.getStart_level());
        }

        upgrades.add(upgradeMap);


        Pickaxe pickaxe = new Pickaxe(
                player,
                String.format("§fКирка §b%s", name),
                0,
                0,
                upgrades
        );

        pickaxes.put(name, pickaxe);
        opPlayer.set(opPlayer.getItems().getPickaxe(), 1);
    }

    public void load() {
        OpPlayer opPlayer = new OpPlayer(player);
        String statsSQL = (String) PlayerDataSQL.get(name, "pickaxe");
        String pickaxeName = "null";
        double exp = 0.0;
        double level = 0.0;

        for (String stats : statsSQL.split(",")) {
            String[] args = stats.split("=");

            String arg_0 = args[0];
            String arg_1 = args[1];

            switch (arg_0) {
                case "name":
                    pickaxeName = arg_1;
                    break;
                case "exp":
                    exp = Double.parseDouble(arg_1);
                    break;
                case "level":
                    level = Double.parseDouble(arg_1);
                    break;
            }

            Upgrade upgrade = getUpgradeFromString(arg_0);

            if (upgrade == null)
                continue;

            double count = Double.parseDouble(arg_1);

            upgradeMap.put(upgrade, count);
        }

        upgrades.add(upgradeMap);

        Pickaxe pickaxe = new Pickaxe(player, pickaxeName, exp, level, upgrades);
        pickaxes.put(name, pickaxe);
        opPlayer.set(opPlayer.getItems().getPickaxe(), 1);
    }

    public void unload() {
        if (!pickaxes.isEmpty())
            pickaxes.remove(name);
    }

    public String getStats() {
        StringBuilder builder = new StringBuilder();
        Pickaxe pickaxe = pickaxes.get(name);

        builder.append(String.format("name=%s", pickaxe.getName()));
        builder.append(String.format("exp=%s", StringUtils._fixDouble(0, pickaxe.getExp())));
        builder.append(String.format("level=%s", StringUtils._fixDouble(0, pickaxe.getLevel())));

        for (Upgrade upgrade : Upgrade.values()) {
            Map<Upgrade, Double> upgradesMap = pickaxes.get(name).getUpgrades().get(upgrade.ordinal());

            String format = "%s=%s,";

            if (upgrade.ordinal() == Upgrade.values().length -1) {
                format = format.replace(",", "");
            }

            builder.append(String.format(format, ChatColor.stripColor(upgrade.getName()), StringUtils._fixDouble(0, upgradesMap.get(upgrade))));
        }

        return builder.toString();
    }

    public Upgrade getUpgradeFromString(String upgrade) {
        for (Upgrade upgrades : Upgrade.values()) {
            if (upgrades == Upgrade.valueOf(upgrade)) {
                return upgrades;
            }
        }

        return null;
    }
}
