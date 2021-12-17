package ru.smole.data.cases;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Chest;
import org.bukkit.material.Directional;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.smole.utils.config.ConfigUtils;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;

@Data
@Getter
public class Case {

    public static Map<String, Case> cases = new HashMap<>();

    private String id;
    private String name;
    private String key;
    private Location location;
    private List<CaseItem> caseItems;

    public Case(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name");
        this.key = section.getString("key").toUpperCase();
        this.location = ConfigUtils.loadLocationFromConfigurationSection(section);

        SimpleHolographic simpleHolographic = new SimpleHolographic(new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() - 0.2, location.getBlockZ() + 0.5));
        simpleHolographic.addTextLine(name);
        simpleHolographic.addTextLine("§bЛКМ §f- Просмотр информации о кейсе");
        simpleHolographic.addTextLine("§bПКМ §f- Открыть кейс на ВСЕ ключи");
        simpleHolographic.addTextLine("§8(сообщений о выпавших предметов нет, чтобы не заполнять чат)");

        LeaderBoard.holograms.add(simpleHolographic);
        this.caseItems = new ArrayList<>();

        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null && itemsSection.getKeys(false).size() > 0) {
            itemsSection.getKeys(false).forEach((item) -> {
                try {
                    this.caseItems.add(new CaseItem(itemsSection.getConfigurationSection(item)));
                } catch (NullPointerException var5) {
                    System.out.println("Error while loading case item in custom case " + id + " with item id " + item);
                }

            });
        }


        Block block = location.getBlock();
        block.setType(Material.CHEST);

        BlockState state = block.getState();
        Directional data = (Directional) state.getData();

        data.setFacingDirection(BlockFace.WEST);

        if (key.equals("TOKEN") || key.equals("LEGENDARY") || key.equals("MYTHICAL")) {
            data.setFacingDirection(BlockFace.NORTH);
        }

        if (key.equals("MINE")) {
            data.setFacingDirection(BlockFace.SOUTH);
        }

        state.update();

        cases.put(id, this);
    }

    public static Case getCustomCaseByLocation(Block block) {
        for (Case customCase : cases.values())
            if (block.equals(customCase.getLocation().getBlock()))
                return customCase;

        return null;
    }

    public void open(Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        String playerName = player.getName();

        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemHand = playerInventory.getItemInMainHand();
        int count = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = playerInventory.getItem(i);

            if (itemStack == null)
                continue;

            if (!itemHand.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName()))
                continue;

            count = count + itemStack.getAmount();
            itemStack.setAmount(0);
        }

        for (int i = 0; i < count; i++) {
            CaseItem caseItem = getRandomItem();
            CaseItem.CaseItemType caseItemType = caseItem.getType();

            if (caseItem.getChance() <= 0.005) {
                ChatUtil.broadcast(OpPrison.PREFIX + "Игрок %s выбил с §r%sа: %s",
                        playerName, name, caseItemType == CaseItem.CaseItemType.ITEM ? caseItem.getItemStack().getItemMeta().getDisplayName() == null ? caseItem.getItemStack().getType().name().replaceAll("_", " ") : caseItem.getItemStack().getItemMeta().getDisplayName() : "не установлено");
            }

            if (caseItemType == CaseItem.CaseItemType.ITEM) {
                opPlayer.add(caseItem.get(playerName).clone());
                continue;
            }


            caseItem.get(playerName);
        }
    }

    public CaseItem getRandomItem() {
        Random random = new Random();
        CaseItem returnedCaseItem = null;

        while (returnedCaseItem == null) {
            CaseItem caseItem = caseItems.get(random.nextInt(caseItems.size()));

            if (random.nextFloat() <= caseItem.getChance())
                returnedCaseItem = caseItem;
        }

        return returnedCaseItem;
    }
}
