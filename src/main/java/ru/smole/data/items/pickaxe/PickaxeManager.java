package ru.smole.data.items.pickaxe;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.items.Items;
import ru.smole.data.mysql.PlayerDataSQL;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PickaxeManager {

    public static @Getter Map<String, Pickaxe> pickaxes;
    private final Player player;

    private final String name;
    private Map<Upgrade, Upgrade.UpgradeStat> upgradeMap;

    public PickaxeManager(Player player) {
        this.player = player;
        name = player.getName();
    }

    public void create() {
        upgradeMap = new HashMap<>();

        for (Upgrade upgrade : Upgrade.values())
            upgradeMap.put(upgrade, new Upgrade.UpgradeStat(upgrade.getStart_level(), true, upgrade.isNeedMessage(), upgrade != Upgrade.JACK_HAMMER));


        Pickaxe pickaxe = new Pickaxe(
                player,
                String.format("§b%s", name),
                0,
                0,
                upgradeMap
        );

        pickaxes.put(name, pickaxe);

        OpPlayer.add(player, Items.getItem("pickaxe", name));
    }

    public void load(String statsSQL) {
        if (statsSQL == null || statsSQL.isEmpty()) {
            create();
            return;
        }

        upgradeMap = new HashMap<>();

        String pickaxeName = "null";
        double exp = 0.0;
        double level = 0.0;

        for (String stats : statsSQL.split(",")) {
            String[] args = stats.split("-");

            String arg_0 = args[0];
            String arg_1 = args[1];

            switch (arg_0) {
                case "name":
                    pickaxeName = arg_1;
                    continue;
                case "exp":
                    exp = Double.parseDouble(arg_1);
                    continue;
                case "level":
                    level = Double.parseDouble(arg_1);
                    continue;
            }

            Upgrade upgrade = getUpgradeFromString(arg_0);

            if (upgrade == null)
                continue;

            double count = Double.parseDouble(arg_1);
            boolean is = args.length <= 3 || Boolean.parseBoolean(args[3]);
            boolean isMes = args.length <= 4 ? upgrade.isNeedMessage() : Boolean.parseBoolean(args[4]);
            upgradeMap.put(upgrade, new Upgrade.UpgradeStat(count,
                    is,
                    isMes,
                    Boolean.parseBoolean(args[2])));
        }

        for (Upgrade upgrade : Upgrade.values()) {
            if (upgradeMap.get(upgrade) == null) {
                upgradeMap.put(upgrade, new Upgrade.UpgradeStat(0, true, upgrade.isNeedMessage(), upgrade != Upgrade.JACK_HAMMER));
            }
        }

        Pickaxe pickaxe = new Pickaxe(player, pickaxeName, exp, level, upgradeMap);
        pickaxes.put(name, pickaxe);

        OpPlayer.add(player, Items.getItem("pickaxe", name));
    }

    public void unload() {
        if (!pickaxes.isEmpty())
            pickaxes.remove(name);

        Arrays.stream(player.getInventory().getStorageContents())
                .parallel()
                .filter(itemStack1 -> itemStack1.getType() == Material.DIAMOND_PICKAXE)
                .forEach(itemStack1 -> {
                    player.getInventory().remove(itemStack1);
                });
    }

    public String getStats() {
        StringBuilder builder = new StringBuilder();
        Pickaxe pickaxe = pickaxes.get(name);

        builder.append(String.format("name-%s,", pickaxe.getName().replace("§fКирка ", "")));
        builder.append(String.format("exp-%s,", StringUtils._fixDouble(0, pickaxe.getExp())));
        builder.append(String.format("level-%s,", StringUtils._fixDouble(0, pickaxe.getLevel())));

        for (Upgrade upgrade : Upgrade.values()) {
            Map<Upgrade, Upgrade.UpgradeStat> upgradesMap = pickaxes.get(name).getUpgrades();

            String format = "%s-%s-%s-%s-%s,";

            if (upgrade.ordinal() == Upgrade.values().length -1) {
                format = format.replace(",", "");
            }

            Upgrade.UpgradeStat stat = upgradesMap.get(upgrade);
            builder.append(String.format(format, upgrade.name(), StringUtils._fixDouble(0, stat.getCount()), stat.isCompleteQ(), stat.isIs(), stat.isMessage()));
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
