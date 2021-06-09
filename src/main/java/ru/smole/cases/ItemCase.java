package ru.smole.cases;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data public class ItemCase {

    private String name;
    private ItemStack stack;
    private double chance;

    public ItemCase(String name, ItemStack stack, double chance) {
        this.name = name;
        this.stack = stack;
        this.chance = chance;
    }
}
