package ru.smole.data.cases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.commands.StatsCommand;
import ru.smole.data.player.PlayerData;
import ru.smole.data.items.Items;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;

@Data public class CaseItem {

    private CaseItemType type;
    private double chance;
    private String name;

    protected ItemStack itemStack;
    protected StatsCommand.Stat stat;
    protected double value;

    public CaseItem(ConfigurationSection section) {
        this.type = CaseItemType.valueOf(section.getString("type").toUpperCase());

        ConfigurationSection op_item = section.getConfigurationSection("op-item");
        switch (type) {
            case ITEM:
                name = op_item.getString("name");
                value = op_item.getDouble("value");

                itemStack = Items.getItem(name, value);
                if (itemStack == null)
                    return;

                break;
            case VAULT:
                stat = StatsCommand.Stat.valueOf(op_item.getString("name").toUpperCase());
                value = op_item.getDouble("value");

                break;
        }

        this.chance = section.getDouble("chance");
    }

    public ItemStack get(String playerName) {
        switch (type) {
            case ITEM:
                return itemStack;
            case VAULT:
                PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName);

                switch (stat) {
                    case MONEY:
                        playerData.addMoney(value);
                        itemStack = ApiManager.newItemBuilder(Material.EMERALD).setName("§a$" + StringUtils.formatDouble(2, value)).setAmount(1).build();
                        break;

                    case TOKEN:
                        playerData.addToken(value);
                        itemStack = ApiManager.newItemBuilder(Material.MAGMA_CREAM).setName("§e⛃" + StringUtils.formatDouble(2, value)).setAmount(1).build();
                        break;

                    case MULTIPLIER:
                        itemStack = ApiManager.newItemBuilder(Material.EYE_OF_ENDER).setName("§dx" + StringUtils.formatDouble(2, value)).setAmount(1).build();
                        playerData.addMultiplier(value);
                        break;
                }

                return itemStack;
            }

        return null;
    }

    public ItemStack get() {
        switch (type) {
            case ITEM:
                return itemStack;
            case VAULT:

                switch (stat) {
                    case MONEY:
                        itemStack = ApiManager.newItemBuilder(Material.EMERALD).setName("§a$" + StringUtils.formatDouble(2, value)).setAmount(1).build();
                        break;

                    case TOKEN:
                        itemStack = ApiManager.newItemBuilder(Material.MAGMA_CREAM).setName("§e⛃" + StringUtils.formatDouble(2, value)).setAmount(1).build();
                        break;

                    case MULTIPLIER:
                        itemStack = ApiManager.newItemBuilder(Material.EYE_OF_ENDER).setName("§dx" + StringUtils.formatDouble(2, value)).setAmount(1).build();
                        break;
                }

                return itemStack;
        }

        return null;
    }

    @Getter
    @AllArgsConstructor
    public enum CaseItemType {

        ITEM(),
        VAULT()

    }
}
