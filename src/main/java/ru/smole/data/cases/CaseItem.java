package ru.smole.data.cases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.commands._givebooster;
import ru.smole.OpPrison;
import ru.smole.commands.StatsCommand;
import ru.smole.data.OpPlayer;
import ru.smole.data.PlayerData;
import ru.smole.data.items.Items;
import ru.xfenilafs.core.ApiManager;

@Data public class CaseItem {

    private String name;
    private CaseItemType type;
    private Material material;
    private int amount;
    private double chance;

    protected ItemStack itemStack;
    protected StatsCommand.Stat stat;
    protected double value;

    public CaseItem(ConfigurationSection section) {
        this.name = section.getString("name");
        this.type = CaseItemType.valueOf(section.getString("type").toUpperCase());

        switch (type) {
            case ITEM:
                ConfigurationSection op_item = section.getConfigurationSection("op-item");

                String name = op_item.getString("name");
                double value1 = op_item.getDouble("value");

                itemStack = Items.getItem(name, value1);
                break;
            case VAULT:
                ConfigurationSection vault_item = section.getConfigurationSection("vault-item");

                stat = StatsCommand.Stat.valueOf(vault_item.getString("stat").toUpperCase());
                value = vault_item.getDouble("value");
        }

        this.amount = section.getInt("amount");
        this.chance = section.getDouble("chance");
        this.material = Material.valueOf(section.getString("material").toUpperCase());
    }

    public Object get(String playerName) {
        switch (type) {
            case ITEM:
                return itemStack;
            case VAULT:
                PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName);

                switch (stat) {
                    case MONEY:
                        playerData.addMoney(value);
                        break;

                    case TOKEN:
                        playerData.addToken(value);
                        break;

                    case MULTIPLIER:
                        playerData.addMultiplier(value);
                        break;
                }

                return value;
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
