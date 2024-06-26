package ru.smole.data.items.crates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Crate {

    public static Map<String, Crate> crates = new HashMap<>();
    public static List<CrateItem> items;

    private String name;
    private Type type;
    private List<CrateItem> crateItems;

    public Crate(String id, ConfigurationSection section) {
        items = new ArrayList<>();
        this.type = Type.valueOf(section.getString("type").toUpperCase());
        this.name = type.getName();

        crateItems = new ArrayList<>();
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null && itemsSection.getKeys(false).size() > 0) {
            itemsSection.getKeys(false).forEach((item) -> {
                try {
                    crateItems.add(new CrateItem(itemsSection.getConfigurationSection(item)));
                } catch (NullPointerException var5) {
                    System.out.println("Error while loading crate item in custom crate " + type.getName() + " with item id " + item);
                }

            });
        }

        crates.put(id, this);
    }

    public void open(Player player) {
        if (type.isMessage())
            Bukkit.getOnlinePlayers().forEach(onPlayer ->
                    ChatUtil.sendMessage(onPlayer, OpPrison.PREFIX +
                            String.format("&a%s открывает %s&f...",
                                    player.getName(), type.getName())));

        new CrateGui().openInventory(player);
    }

    public CrateItem getRandomItem(boolean def) {
        Random random = new Random();
        CrateItem returnCrateItem = null;

        if (def) {
            List<CrateItem> defList =
                    crateItems
                            .stream()
                            .filter(crateItem -> crateItem.getRare() == CrateItem.Rare.EPIC ||
                                    crateItem.getRare() == CrateItem.Rare.LEGENDARY ||
                                    crateItem.getRare() == CrateItem.Rare.MYTHICAL
                            )
                            .collect(Collectors.toList());

            while (returnCrateItem == null) {
                for (CrateItem crateItem : defList) {
                    if (random.nextFloat() <= crateItem.getRare().getChance())
                        returnCrateItem = crateItem;
                }
            }

            return returnCrateItem;
        }

        List<CrateItem> unDefList = crateItems.stream().filter(
                crateItem ->
                        crateItem.getRare() == CrateItem.Rare.COMMON ||
                                crateItem.getRare() == CrateItem.Rare.RARE
        ).collect(Collectors.toList());

        while (returnCrateItem == null) {
            for (CrateItem crateItem : unDefList) {
                if (random.nextFloat() <= crateItem.getRare().getChance())
                    returnCrateItem = crateItem;
            }
        }

        return returnCrateItem;
    }

    @AllArgsConstructor
    public enum Type {

        ARMOR("§bЯЩИК С БРОНЁЙ", false),
        RARE_ARMOR("§b§lРЕДКИЙ §r§bЯЩИК С БРОНЁЙ", false),
        POTION("§cЯЩИК С ЗЕЛЬЯМИ", false),
        LOOT_BOX("§6ЛУТБОКС", false),
        MONTHLY("§aЯЩИК МЕСЯЦА", true);

        private final @Getter String name;
        private final @Getter boolean message;

        public ItemStack getStack() {
            List<String> lore = new ArrayList<>();

            lore.add("§7Нажмите для активации");
            lore.add("");

           Crate.crates.get(name().toLowerCase()).getCrateItems().forEach(crateItem -> {
                ItemStack itemStack = crateItem.get();

                if (itemStack == null) {
                    System.out.println(crateItem.getType() == CrateItem.CrateItemType.ITEM ? crateItem.getName() : crateItem.getStat().name() + " | " + crateItem.getValue());
                    return;
                }

                lore.add(
                        crateItem.getRare().getName() + " " + itemStack.getItemMeta().getDisplayName() + " §fx" + itemStack.getAmount()
                );
            });

            return ApiManager
                    .newItemBuilder(Material.ENDER_CHEST)
                    .setName(name)
                    .setLore(lore)
                    .build();
        }
    }

    // inventory

    public class CrateGui extends BaseSimpleInventory {
        public CrateGui() {
            super(3, "§bЗаберите Награду");
        }

        @Override
        public void drawInventory(@NonNull Player player) {
            OpPlayer opPlayer = new OpPlayer(player);

            switch (type) {
                case LOOT_BOX:
                    for (int i = 0; i < 2; i++) {
                        setDefaultReward(opPlayer, i);
                    }

                    setFinalReward(opPlayer, 15);
                    setGlassPanel();

                    break;
                case MONTHLY:
                    for (int i = 0; i < 5; i++) {
                        setDefaultReward(opPlayer, i);
                    }

                    setFinalReward(opPlayer, 22, 24);
                    setGlassPanel();

                    break;
                case ARMOR:
                    setDefaultReward(opPlayer, 1);
                    setFinalReward(opPlayer, 15);
                    setGlassPanel();

                    break;
                case POTION:
                    setDefaultReward(opPlayer, 1);
                    setFinalReward(opPlayer, 15);
                    setGlassPanel();

                    break;
                case RARE_ARMOR:
                    for (int i = 0; i < 2; i++) {
                        setDefaultReward(opPlayer, i);
                    }

                    setFinalReward(opPlayer, 15);
                    setGlassPanel();

                    break;
            }
        }

        private void setDefaultReward(OpPlayer opPlayer, int i) {
            int slot = 12 + i;

            CrateItem crateItem = getRandomItem(false);
            ItemStack itemStack = crateItem.get(opPlayer.getPlayer().getName());

            items.add(crateItem);

            addItem(
                    slot,
                    ApiManager
                            .newItemBuilder(Material.LADDER)
                            .setName("§f??????")
                            .setLore("§7Нажмите, чтобы забрать награду")
                            .build(),
                    (baseInventory, inventoryClickEvent) -> {
                        Inventory inv = inventoryClickEvent.getClickedInventory();
                        int clickedSlot = inventoryClickEvent.getSlot();
                        ItemStack clickItem = inv.getItem(clickedSlot);

                        if (clickItem.getType() != Material.LADDER)
                            return;

                        inv.setItem(clickedSlot, itemStack);

                        if (crateItem.getType() == CrateItem.CrateItemType.ITEM)
                            opPlayer.add(itemStack.clone());

                        items.remove(crateItem);
                    });
        }

        private void setFinalReward(OpPlayer opPlayer, int... i) {
            for (int f : i) {
                CrateItem crateItem = getRandomItem(true);
                ItemStack itemStack = crateItem.get(opPlayer.getPlayer().getName());

                items.add(crateItem);

                addItem(
                        f,
                        ApiManager
                                .newItemBuilder(Material.IRON_FENCE)
                                .setName("§c??????")
                                .setLore("§7Нажмите, чтобы забрать финальную награду")
                                .build(),
                        (baseInventory, inventoryClickEvent) -> {
                            Inventory inv = inventoryClickEvent.getClickedInventory();
                            int clickedSlot = inventoryClickEvent.getSlot();
                            ItemStack clickItem = inv.getItem(clickedSlot);

                            if (clickItem.getType() != Material.IRON_FENCE)
                                return;

                            inv.setItem(clickedSlot, itemStack);

                            if (crateItem.getType() == CrateItem.CrateItemType.ITEM)
                                opPlayer.add(itemStack.clone());

                            items.remove(crateItem);

                            if (type.isMessage())
                                Bukkit.getOnlinePlayers().forEach(onPlayer -> crateItem.sendMessage(onPlayer, opPlayer.getPlayer(), name));
                        });
            }
        }

        private void setGlassPanel() {
            for (int i = 1; i <= getInventoryRows() * 9; i++) {
                BaseInventoryItem item = getInventoryInfo().getItem(i - 2);

                if (item == null)
                    addItem(
                            i,
                            ApiManager
                                    .newItemBuilder(Material.STAINED_GLASS_PANE)
                                    .setName(" ")
                                    .setDurability(7)
                                    .build()
                    );
            }
        }
    }
}
