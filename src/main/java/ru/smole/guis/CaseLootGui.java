package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.cases.Case;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;

public class CaseLootGui extends BaseSimpleInventory {
    private final Case customCase;

    public CaseLootGui(Case customCase) {
        super(3, "Шансы выпадения");
        this.customCase = customCase;
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        for (int i = 0; i < inventory.getSize(); ++i) {
            if (i < 10 || i > inventory.getSize() - 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45) {
                this.inventory.setItem(
                        i,
                        ApiManager.newItemBuilder(Material.STAINED_GLASS_PANE)
                                .setName(" ")
                                .setDurability(7)
                                .build()
                );
            }
        }

        if (!customCase.getItems().isEmpty()) {
            customCase.getItems().forEach((caseItem) -> {
                double chance = caseItem.getChance() * 100.0D;
                for (int i = 0; i < customCase.getItems().size(); i++) {
                    addItem(
                            i,
                            ApiManager.newItemBuilder(caseItem.getMaterial())
                                    .setName(caseItem.getName())
                                    .setLore(
                                            "",
                                            "Шанс выпадения: " + StringUtils.formatDouble(chance)
                                    )
                                    .setAmount(caseItem.getAmount())
                                    .build()
                    );
                }
            });
        }

    }
}
