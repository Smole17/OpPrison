package ru.smole.data.items.crates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.commands.StatsCommand;
import ru.smole.data.player.PlayerData;
import ru.smole.data.items.Items;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.util.ChatUtil;

@Data
public class CrateItem {

    private Rare rare;
    private CrateItemType type;
    private double chance;
    private String name;

    protected ItemStack itemStack;
    protected StatsCommand.Stat stat;
    protected double value;
    protected String hValue = "null";

    public CrateItem(ConfigurationSection section) {
        this.rare = Rare.valueOf(section.getString("rare").toUpperCase());
        this.type = CrateItemType.valueOf(section.getString("type").toUpperCase());

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

                if (value == 0)
                    hValue = op_item.getString("value");
        }

        this.chance = section.getDouble("chance");
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
                    case ACCESS:
                        itemStack = ApiManager.newItemBuilder(Material.STORAGE_MINECART)
                                .setName(String.format("Доступ к %s набору §8(/kit)", hValue.replace("season", "§bСезонному"))).setAmount(1).build();
                        break;
                }

                return itemStack;
        }

        return null;
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

                    case ACCESS:
                        itemStack = ApiManager.newItemBuilder(Material.STORAGE_MINECART)
                                .setName(String.format("§fДоступ к %s §fнабору §8(/kit)", hValue.replace("season", "Сезонному"))).setAmount(1).build();
                        playerData.getAccess().add(hValue);
                        break;
                }

                return itemStack;
        }

        return null;
    }

    public void sendMessage(Player sender, Player player, String crateName) {
            ChatUtil.sendMessage(sender, OpPrison.PREFIX +
                    String.format("&a%s открыл &r%s &aи получил &r%s %s &fx%s",
                            player.getName(),
                            crateName,
                            rare.getName(),
                            itemStack.getItemMeta().getDisplayName(),
                            itemStack.getAmount()
                    ));
    }

    @Getter
    @AllArgsConstructor
    public enum Rare {

        COMMON("§7ОБЫЧНЫЙ", 0.9),
        RARE("§9РЕДКИЙ", 0.20),
        EPIC("§5ЭПИЧЕСКИЙ", 0.10),
        LEGENDARY("§6ЛЕГЕНДАРНЫЙ", 0.05),
        MYTHICAL("§cМИФИЧЕСКИЙ", 0.005);

        private String name;
        private double chance;
    }

    @Getter
    @AllArgsConstructor
    public enum CrateItemType {

        ITEM(),
        VAULT()

    }
}
