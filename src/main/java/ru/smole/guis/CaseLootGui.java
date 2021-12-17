package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.cases.Case;
import ru.smole.data.cases.CaseItem;
import ru.smole.data.items.Items;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;

import java.util.List;

public class CaseLootGui extends BaseSimpleInventory {
    private final Case customCase;

    public CaseLootGui(Case customCase) {
        super(6, "§7Шансы Выпадения");
        this.customCase = customCase;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        for (int i = 0; i <= inventory.getSize(); ++i) {
            if (i > 45 && i < 55 && i != 50) {
                addItem(i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build()
                );
            }
        }

        Items.Key needKey = Items.Key.getKeyFromString(customCase.getKey());

        if (needKey == null)
            return;

        addItem(50,
                ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK)
                        .setName(customCase.getName())
                        .setLore(
                                "§fДля открытия этого кейса",
                                "§fвам нужен " + needKey.getStack().getItemMeta().getDisplayName()
                        )
                        .build()
        );

        List<CaseItem> items = customCase.getCaseItems();

        if (!items.isEmpty()) {
            int i = 1;

            for (CaseItem caseItem : items) {
                double chance = caseItem.getChance() * 100.0D;
                ItemStack itemStack = caseItem.get();

                if (itemStack == null) {
                    System.out.println(caseItem.getType() == CaseItem.CaseItemType.ITEM ? caseItem.getName() : caseItem.getStat().name() + " | " + caseItem.getValue());
                    return;
                }

                if (!itemStack.hasItemMeta()) {
                    System.out.println(caseItem.getType() == CaseItem.CaseItemType.ITEM ? caseItem.getName() : caseItem.getStat().name() + " | " + caseItem.getValue());
                    return;
                }

                String p = String.valueOf(chance).split("\\.")[1];
                int fix = p.equals("0") ? 0 : p.length();

                addItem(
                        i,
                        ApiManager.newItemBuilder(itemStack)
                                .setLore(
                                       String.format("§fШанс выпадения: §b%s%%",
                                               StringUtils._fixDouble(fix, chance))
                                )
                                .setAmount(itemStack.getAmount())
                                .build()
                );

                i = i + 1;
            }
        }
    }
}
