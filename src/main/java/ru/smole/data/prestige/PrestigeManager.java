package ru.smole.data.prestige;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

public class PrestigeManager {

    private Player player;
    private PlayerData playerData;

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
        double upped = prestige + 1;
        double money = playerData.getMoney();

        double c = 10000000D;
        double cost = prestige == 0 ? c : upped * 1.05F * c;

        if (cost > money) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вам не хватает: &a$%s", StringUtils.replaceComma(cost - money));
            return;
        }

        switch (i) {
            case 1:
                playerData.setMoney(money - cost);
                playerData.setPrestige(upped);

                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы прокачали престиж до: &b%s", StringUtils._fixDouble(0, upped));
                return;
            case 2:
                double cost2 = 0;
                double need = 0;

                while (money >= need) {
                    if (need >= money)
                        break;

                    need = need + (upped * 1.05F * c);

                    if (need >= money)
                        break;

                    upped = upped + 1;
                    cost2 = need;
                }

                playerData.setMoney(money - cost2);
                playerData.setPrestige(upped);

                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы прокачали престиж до: &b%s", StringUtils._fixDouble(0, playerData.getPrestige()));
        }
    }
}
