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

        MANTLE("§7", 0),
        EARTH("§8", 259200000),
        AQUA("§9", 345600000),
        AIR("§f", 432000000),
        SKY("§b", 518400000),
        COSMOS("§5", 604800000),
        SUN("§e", 691200000),
        GALAXY("§d", 777600000),
        UNIVERSE("§0", 864000000),
        ADMIN("§c", 0);

        private @Getter String color;
        private @Getter long kitTime;

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
