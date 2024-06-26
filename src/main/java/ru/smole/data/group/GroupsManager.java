package ru.smole.data.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.xfenilafs.core.ApiManager;

public class GroupsManager {

    private Player player;

    public GroupsManager(Player player) {
        this.player = player;
    }

    public boolean isCan(Group group) {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        int ordinal = playerData.getGroup().ordinal();

        return ordinal >= group.ordinal();
    }

    @AllArgsConstructor
    public enum Group {

        MANTLE("§7", 100),
        EARTH("§8", 115),
        AQUA("§9", 125),
        AIR("§f", 135),
        SKY("§b", 145),
        COSMOS("§5", 160),
        SUN("§e", 175),
        GALAXY("§d", 190),
        UNIVERSE("§0", 210),
        ADMIN("§c", 9999);

        private final @Getter String color;
        private final @Getter double multiplier;

        public String getName() {
            return color + name();
        }

        public boolean isCan(Group group) {
            return group.ordinal() >= this.ordinal();
        }

        public ItemStack getStack() {
            return ApiManager
                    .newItemBuilder(Material.PAPER)
                    .setName(getName() + " §fгруппа")
                    .addLore("§7Нажмите для активации")
                    .build();
        }

        public static Group getGroupFromString(String group) {
            for (Group type : Group.values())
                if (type.equals(Group.valueOf(group.toUpperCase())))
                    return type;

            return null;
        }
    }
}
