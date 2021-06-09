package ru.smole.cases;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

@Data public class Case {

    private String name;
    private Location location;
    private List<ItemCase> items;

    public Case(String name, Location location, List<ItemCase> items) {
        this.name = name;
        this.location = location;
        this.items = items;
    }

    public ItemStack getRandomItem(Player player) {
        ItemStack itemStack = null;
        Random random = new Random();

        while(itemStack == null) {
            ItemCase caseItem = items.get(random.nextInt(items.size()));
            double chance = caseItem.getChance();
            if (random.nextFloat() <= chance) {
                itemStack = caseItem.getStack();
                itemStack.setAmount(caseItem.getStack().getAmount());
            }
        }

        return itemStack;
    }
}
