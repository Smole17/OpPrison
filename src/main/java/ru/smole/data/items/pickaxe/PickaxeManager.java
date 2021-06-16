package ru.smole.data.items.pickaxe;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
    }

    public void load() {
        for (Upgrade upgrade : Upgrade.values()) {

        }
    }

    public void unload() {

    }

    public String getStats() {
        StringBuilder builder = new StringBuilder();
        for (Upgrade upgrade : Upgrade.values()) {
            Map<Upgrade, Double> upgradesMap = pickaxes.get(name).getUpgrades().get(upgrade.ordinal());

            String format = "%s=%f,";

            if (upgrade.ordinal() == Upgrade.values().length -1) {
                format = format.replace(",", "");
            }

            builder.append(String.format(format, ChatColor.stripColor(upgrade.getName()), StringUtils.fixDouble(0, upgradesMap.get(upgrade))));
        }

        return builder.toString();
    }
}
