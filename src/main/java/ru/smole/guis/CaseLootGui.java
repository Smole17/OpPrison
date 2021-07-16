package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.data.cases.Case;
import ru.smole.data.cases.CaseItem;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.CrateItem;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryClickHandler;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;

import java.util.List;

public class CaseLootGui extends BaseSimpleInventory {
    private Case customCase;

    public CaseLootGui(Case customCase) {
        super(4, "Шансы выпадения");
        this.customCase = customCase;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        for (int i = 0; i <= inventory.getSize(); ++i) {
            if (i > 27 && i < 37 && i != 32) {
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

        addItem(32,
                ApiManager.newItemBuilder(Material.TRIPWIRE_HOOK)
                        .setName(customCase.getName())
                        .setLore(
                                "§fДля открытия этого кейса",
                                "§fвам нужен " + needKey.getStack().getItemMeta().getDisplayName()
                        )
                        .build()
        );

        List<CaseItem> items = customCase.getItems();

        if (!items.isEmpty()) {
            int i = 1;

            for (CaseItem caseItem : items) {
                double chance = caseItem.getChance() * 100.0D;

                addItem(
                        i,
                        ApiManager.newItemBuilder(caseItem.getMaterial())
                                .setName(caseItem.getName())
                                .setLore(
                                        "§fШанс выпадения: §b" + StringUtils.fixDouble(1, chance)
                                )
                                .setAmount(caseItem.getAmount())
                                .build()
                );

                i = i + 1;
            }
        }
    }
}
