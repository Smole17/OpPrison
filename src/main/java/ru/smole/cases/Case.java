package ru.smole.cases;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.utils.config.ConfigUtils;

import java.util.*;

@Data
@Getter
public class Case {

    public static Map<String, Case> cases = new LinkedHashMap();
    private String id;
    private String key;
    private Location location;
    private List<CaseItem> items;

    public Case(String id, ConfigurationSection section) {
        this.id = id;
        this.key = section.getString("key");
        this.location = ConfigUtils.loadLocationFromConfigurationSection(section);
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null && itemsSection.getKeys(false).size() > 0) {
            itemsSection.getKeys(false).forEach((item) -> {
                try {
                    this.items.add(new CaseItem(itemsSection.getConfigurationSection(item)));
                } catch (NullPointerException var5) {
                    System.out.println("Error while loading case item in custom case " + id + " with item id " + item);
                }

            });
        }

        cases.put(id, this);
    }

    public static Case getCase(String id) {
        return cases.get(id);
    }

    public static Case getCustomCaseByLocation(Block block) {
        for (Case customCase : cases.values()) {
            if (block.getLocation().equals(customCase.getLocation())) {
                return customCase;
            }
        }

        return null;
    }

    public void open(Player player, boolean fastOpen) {
        ItemStack itemHand = player.getInventory().getItemInMainHand();

        if (fastOpen) {
            for (int i = 0; i < itemHand.getAmount(); i++) {
                ItemStack itemStack = getRandomItem();
                if (itemStack != null) {
                    player.getInventory().addItem(itemStack);
                }
                itemHand.setAmount(0);
            }
            return;
        }

        ItemStack itemStack = getRandomItem();
        if (itemStack != null) {
            player.getInventory().addItem(itemStack);
        }
        itemHand.setAmount(itemHand.getAmount() - 1);

    }

    public ItemStack getRandomItem() {
        ItemStack itemStack = null;
        Random random = new Random();

        while (itemStack == null) {
            CaseItem caseItem = items.get(random.nextInt(items.size()));
            double chance = caseItem.getChance();
            if (random.nextFloat() <= chance) {
                itemStack = caseItem.getStack();
                itemStack.setAmount(caseItem.getStack().getAmount());
            }
        }

        return itemStack;
    }
}
