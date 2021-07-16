package ru.smole.data.items.crates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.OpPlayer;
import ru.smole.data.cases.Case;
import ru.smole.data.items.Items;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.ItemUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Crate {

    public static Map<String, Crate> crates = new HashMap<>();
    public static Map<UUID, List<ItemStack>> playerItems = new HashMap<>();
    public static List<CrateItem> crateItems = new ArrayList<>();

    public String name;
    public Type type;
    public List<ItemStack> items;

    public Crate(String name, ConfigurationSection section) {
        items = new ArrayList<>();
        type = Type.valueOf(section.getString("type").toUpperCase());
        this.name = type.getName();
        ConfigurationSection items = section.getConfigurationSection("items");

        for (String item : items.getKeys(false)) {
            CrateItem.Rare itemRare = CrateItem.Rare.valueOf(items.getString(item + ".rare"));
            ConfigurationSection op_item = items.getConfigurationSection(item + ".op-item");

            String itemName = op_item.getString("name");
            ItemStack itemStack;

            if (op_item.getDouble("amount") != 0) {
                double amount = op_item.getDouble("amount");

                itemStack = Items.getItem(itemName, amount);
            } else itemStack = Items.getItem(itemName);


            CrateItem crateItem = new CrateItem(itemRare, itemStack);
            crateItems.add(crateItem);
        }

        crates.put(name, this);
    }

    public void open(Player player) {
        Bukkit.getOnlinePlayers().forEach(onPlayer ->
                ChatUtil.sendMessage(onPlayer, OpPrison.PREFIX +
                        String.format("%s открывает %s&f...",
                                player.getName(), type.getName())));

        new CrateGui(type.getName()).openInventory(player);
    }

    public CrateItem getRandomItem(boolean def) {
        Random random = new Random();
        CrateItem returnCrateItem = null;

        if (def) {
            List<CrateItem> defList = crateItems.stream().filter(
                    crateItem ->
                            crateItem.getRare() == CrateItem.Rare.EPIC ||
                                    crateItem.getRare() == CrateItem.Rare.LEGENDARY ||
                                    crateItem.getRare() == CrateItem.Rare.MYTHICAL
            ).collect(Collectors.toList());

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

        LOOT_BOX("§6ЛУТБОКС"),
        MONTHLY("§fКЕЙС МЕСЯЦА");

        private @Getter String name;

        public ItemStack getStack() {
            List<String> lore = new ArrayList<>();

            lore.add("§7Нажмите для активации");
            lore.add("");

            crateItems.forEach(
                    crateItem ->
                            lore.add(
                                    crateItem.getRare().getName() + " " + crateItem.getItemStack().getItemMeta().getDisplayName() + " §fx" + crateItem.getItemStack().getAmount())
            );

            return ApiManager
                    .newItemBuilder(Material.ENDER_CHEST)
                    .setName(name)
                    .setLore(lore)
                    .build();
        }
    }

    // inventory

    public class CrateGui extends BaseSimpleInventory {
        public CrateGui(String name) {
            super(3, name);
        }

        @Override
        public void drawInventory(@NonNull Player player) {
            OpPlayer opPlayer = new OpPlayer(player);

            switch (type) {
                case LOOT_BOX:
                    for (int i = 0; i < 2; i++) {
                        setDefaultReward(opPlayer, i);
                    }

                    setFinalReward(opPlayer);
                    setGlassPanel();

                    break;
                case MONTHLY:
                    for (int i = 0; i < 6; i++) {
                        setDefaultReward(opPlayer, i);
                    }

                    setFinalReward(opPlayer);
                    setGlassPanel();

                    break;
            }

            playerItems.put(opPlayer.getPlayer().getUniqueId(), items);
        }

        private void setDefaultReward(OpPlayer opPlayer, int i) {
            int slot = 12 + i;

            CrateItem crateItem = getRandomItem(false);
            ItemStack itemStack = crateItem.getItemStack();

            items.add(itemStack);

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
                        opPlayer.add(itemStack);
                        items.remove(itemStack);
                    });
        }

        private void setFinalReward(OpPlayer opPlayer) {
            CrateItem crateItem = getRandomItem(true);
            ItemStack itemStack = crateItem.getItemStack();

            items.add(itemStack);

            addItem(
                    15,
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

                        inv.remove(clickItem);
                        inv.setItem(clickedSlot, itemStack);
                        opPlayer.add(itemStack);
                        items.remove(itemStack);

                        Bukkit.getOnlinePlayers().forEach(onPlayer -> crateItem.sendMessage(onPlayer, opPlayer.getPlayer(), name));
                    });
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
