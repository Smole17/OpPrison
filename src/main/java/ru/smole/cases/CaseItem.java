package ru.smole.cases;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import ru.xfenilafs.core.ApiManager;

@Data public class CaseItem {

    private String name;
    private Material material;
    private ItemStack stack;
    private int amount;
    private double chance;

    public CaseItem(ConfigurationSection section) {
        this.name = section.getString("name");
        this.amount = section.getInt("amount");
        this.chance = section.getDouble("chance");
        this.material = Material.valueOf(section.getString("material"));

        this.stack = ApiManager.newItemBuilder(material).setName(name).build();
    }
}
