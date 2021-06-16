package ru.smole.guis;

import lombok.NonNull;
import org.bukkit.entity.Player;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.player.OpPlayer;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;

public class PickaxeGui extends BaseSimpleInventory {
    public PickaxeGui() {
        super(6, "Прокачка кирки");
    }

    @Override
    public void drawInventory(@NonNull Player player) {
        OpPlayer opPlayer = new OpPlayer(player);
        Pickaxe pickaxe = opPlayer.getPickaxeManager().getPickaxes().get(player.getName());

        for (Upgrade upgrade : Upgrade.values()) {
            double count = pickaxe.getUpgrades().get(upgrade.ordinal()).get(upgrade);
            // потом
        }
    }
}
