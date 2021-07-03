package ru.smole.data.items.crates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.OpPlayer;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.ItemUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public class Crate {

    public String name;
    public Type type;
    public List<CrateItem> crateItems;

    public void open(Player player) {
        switch (type) {
            case LOOTBOX:
                new CrateGui(type.getName());
            case MONTHLY:
                ChatUtil.sendMessage(player, OpPrison.getInstance() +
                        String.format("%s открывает %s",
                                player.getName(), Type.MONTHLY.getName()));
                new CrateGui(type.getName());
        }
    }

    public CrateItem getRandomItem(boolean def) {
        Random random = new Random();

        if (def) {
            Stream<CrateItem> crateItemDef = crateItems
                    .stream()
                    .filter(crateItem
                            -> crateItem.getRare() == CrateItem.Rare.EPIC ||
                            crateItem.getRare() == CrateItem.Rare.LEGENDARY ||
                            crateItem.getRare() == CrateItem.Rare.MYTHICAL);

            CrateItem crateItem = crateItemDef
                    .collect(Collectors.toList())
                    .get(random.nextInt((int) crateItemDef.count()));

            if (random.nextFloat() <= crateItem.getRare().getChance()) {
                return crateItem;
            }

            List<CrateItem> epics = crateItems
                    .stream()
                    .filter(crateItem1 -> crateItem1.getRare() == CrateItem.Rare.EPIC)
                    .collect(Collectors.toList());

            return epics.get(random.nextInt(epics.size()));
        }

        Stream<CrateItem> crateItemDef = crateItems
                .stream()
                .filter(crateItem
                        -> crateItem.getRare() == CrateItem.Rare.COMMON ||
                        crateItem.getRare() == CrateItem.Rare.RARE);

        CrateItem crateItem = crateItemDef
                .collect(Collectors.toList())
                .get(random.nextInt((int) crateItemDef.count()));

        if (random.nextFloat() <= crateItem.getRare().getChance()) {
            return crateItem;
        }

        List<CrateItem> commons = crateItemDef
                .filter(crateItem1 -> crateItem1.getRare() == CrateItem.Rare.COMMON)
                .collect(Collectors.toList());

        return commons.get(random.nextInt(commons.size()));
    }

    @AllArgsConstructor
    public enum Type {

        LOOTBOX("§6ЛУТБОКС"),
        MONTHLY("§fКЕЙС МЕСЯЦА");

        private @Getter String name;

        public ItemStack getStack() {
            List<String> lore = new ArrayList<>();

            lore.add("§7Нажмите для активации");
            lore.add("");


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
                case LOOTBOX:
                    for (int i = 0; i < 2; i++) {
                        setDefaultReward(opPlayer, i);
                    }


                    setFinalReward(player, opPlayer);
                    setGlassPanel();

                    break;
                case MONTHLY:
                    for (int i = 0; i < 6; i++) {
                        setDefaultReward(opPlayer, i);
                    }

                    setFinalReward(player, opPlayer);
                    setGlassPanel();

                    break;
            }
        }

        private void setDefaultReward(OpPlayer opPlayer, int i) {
            int slot = 12 + i;

            addItem(
                    slot,
                    ApiManager
                            .newItemBuilder(Material.LADDER)
                            .setName("§f??????")
                            .setLore("§7Нажмите, чтобы забрать награду")
                            .build(),
                    (baseInventory, inventoryClickEvent) -> {
                        CrateItem crateItem = getRandomItem(false);
                        ItemStack itemStack = crateItem.getItemStack();

                        inventoryClickEvent.setCurrentItem(itemStack);
                        opPlayer.add(itemStack);
                    });
        }

        private void setFinalReward(@NonNull Player player, OpPlayer opPlayer) {
            addItem(
                    15,
                    ApiManager
                            .newItemBuilder(Material.IRON_FENCE)
                            .setName("§c??????")
                            .setLore("§7Нажмите, чтобы забрать финальную награду")
                            .build(),
                    (baseInventory, inventoryClickEvent) -> {
                        CrateItem crateItem = getRandomItem(true);
                        ItemStack itemStack = crateItem.getItemStack();

                        inventoryClickEvent.setCurrentItem(itemStack);
                        opPlayer.add(itemStack);
                        crateItem.sendMessage(player, name);
                    });
        }

        private void setGlassPanel() {
            for (int i = 1; i < inventoryRows * 9; i++) {
                Material type = getInventory().getItem(i).getType();

                if (type == Material.AIR)
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
