package ru.smole.data.items.crates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.OpPlayer;
import ru.smole.data.items.Items;
import ru.xfenilafs.core.util.ChatUtil;

@Data
@AllArgsConstructor
public class CrateItem {

    private Rare rare;
    private ItemStack itemStack;

    public void sendMessage(Player sender, Player player, String crateName) {
            ChatUtil.sendMessage(sender, OpPrison.PREFIX +
                    String.format("%s открыл %s &fи получил %s %s &fx%s",
                            player.getName(),
                            crateName,
                            rare.getName(),
                            itemStack.getItemMeta().getDisplayName(),
                            itemStack.getAmount()
                    ));
    }

    @Getter
    @AllArgsConstructor
    public enum Rare {

        COMMON("§7ОБЫЧНЫЙ", 0.85),
        RARE("§9РЕДКИЙ", 0.45),
        EPIC("§5ЭПИЧЕСКИЙ", 0.25),
        LEGENDARY("§6ЛЕГЕНДАРНЫЙ", 0.020),
        MYTHICAL("§cМИФИЧЕСКИЙ", 0.01);

        private String name;
        private double chance;
    }
}
