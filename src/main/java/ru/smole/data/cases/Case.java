package ru.smole.data.cases;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.commands.StatsCommand;
import ru.smole.data.PlayerData;
import ru.smole.utils.config.ConfigUtils;
import ru.smole.utils.hologram.Hologram;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Getter
public class Case {

    public static Map<String, Case> cases = new HashMap<>();
    private String id;
    private String name;
    private String key;
    private Location location;
    private List<CaseItem> items;

    public Case(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name");
        this.key = section.getString("key").toUpperCase();
        this.location = ConfigUtils.loadLocationFromConfigurationSection(section);

        String holoId = "case_" + id;
        OpPrison.getInstance().getHologramManager().createHologram(
                holoId,
                new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() - 0.3, location.getBlockZ() + 0.5),
                hologram -> {
                    hologram.addLine(name);
                    hologram.addLine("§fЛКМ - Просмотр выпадаемых вещей");
                    hologram.addLine("§fПКМ - Открыть кейс на ВСЕ ключи");
                }
        );

        LeaderBoard.holograms.add(holoId);
        this.items = new ArrayList<>();

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

    public static Case getCustomCaseByLocation(Block block) {
        for (Case customCase : cases.values())
            if (block.equals(customCase.getLocation().getBlock()))
                return customCase;

        return null;
    }

    public void open(Player player) {
        ItemStack itemHand = player.getInventory().getItemInMainHand();
        String playerName = player.getName();

        CaseItem caseItem = getRandomItem();
        CaseItem.CaseItemType caseItemType = caseItem.getType();

            for (int i = 0; i < itemHand.getAmount(); i++) {
                if (caseItemType != CaseItem.CaseItemType.VAULT) {
                    player.getInventory().addItem((ItemStack) caseItem.get(playerName));
                    return;
                }

                caseItem.get(playerName);
            }

            itemHand.setAmount(0);
    }

    public CaseItem getRandomItem() {
        Random random = new Random();
        CaseItem returnedCaseItem = null;

        while (returnedCaseItem == null) {
            CaseItem caseItem = items.get(random.nextInt(items.size()));

            if (random.nextFloat() <= caseItem.getChance())
                returnedCaseItem = caseItem;
        }

        return returnedCaseItem;
    }
}
