package ru.smole.data.prestige;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

public class PrestigeManager {

    private final Player player;
    private final PlayerData playerData;

    public PrestigeManager(Player player) {
        this.player = player;
        playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
    }

    public void up(int i) {
        switch (i) {
            case 1:
                upgradePrestige(1);
                break;
            case 2:
                upgradePrestige(2);
                break;
        }
    }

    protected void upgradePrestige(int i) {
        double prestige = playerData.getPrestige();
        double upped = 1;
        double money = playerData.getMoney();

        double c = 5000000D;
        double multip = 1.01F;
        double cost = prestige == 0 ? c : upped * multip * c;

        if (cost > money) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "&fВам не хватает: &a$%s", StringUtils.replaceComma(cost - money));
            return;
        }

        switch (i) {
            case 1:
                playerData.setMoney(money - cost);
                playerData.addPrestige(upped);

                ChatUtil.sendMessage(player, OpPrison.PREFIX + "&fВы прокачали престиж до: &b%s", StringUtils._fixDouble(0, playerData.getPrestige()));
                return;
            case 2:
                double cost2 = 0;
                double need = 0;

                while (money >= need) {
                    if (need >= money)
                        break;

                    need = need + (upped * multip * c);

                    if (need >= money)
                        break;

                    upped = upped + 1;
                    cost2 = need;
                }

                playerData.setMoney(money - cost2);
                playerData.addPrestige(upped);

                ChatUtil.sendMessage(player, OpPrison.PREFIX + "&fВы прокачали престиж до: &b%s", StringUtils._fixDouble(0, playerData.getPrestige()));
        }
    }
}
