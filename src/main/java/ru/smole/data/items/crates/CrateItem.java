package ru.smole.data.items.crates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.items.Items;
import ru.xfenilafs.core.util.ChatUtil;

@Data
@AllArgsConstructor
public class CrateItem {

    private String name;
    private Rare rare;
    private ItemStack itemStack;

    public void sendMessage(Player player, String crateName) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX +
                    String.format("%s открыл %s &fи получил %s",
                            player.getName(),
                            crateName,
                            name
                    ));
    }

    @Getter
    @AllArgsConstructor
    public enum Rare {

        COMMON("§7ОБЫЧНЫЙ", 1.0),
        RARE("§9РЕДКИЙ", 0.4),
        EPIC("§5ЭПИЧЕСКИЙ", 0.15),
        LEGENDARY("§6ЛЕГЕНДАРНЫЙ", 0.05),
        MYTHICAL("§cМИФИЧЕСКИЙ", 0.01);

        private String name;
        private double chance;
    }
}
