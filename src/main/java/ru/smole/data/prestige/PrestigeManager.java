package ru.smole.data.prestige;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.rank.Rank;
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
        OpPlayer opPlayer = new OpPlayer(player);
        if (!opPlayer.getRankManager().isNextRank(playerData.getRank())) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы должны достичь ранка %s", ChatColor.translateAlternateColorCodes('&', Rank.Z.getName()));
        }

        switch (i) {
            case 1:
                upgradePrestige(1);
                break;
            case 2:
                while (upgradePrestige(2)) {
                    upgradePrestige(2);
                }

                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы прокачали престиж до: &b%s", StringUtils._fixDouble(0, playerData.getPrestige()));
                break;
        }
    }

    protected boolean upgradePrestige(int i) {
        double prestige = playerData.getPrestige();
        double money = playerData.getMoney();

        double billion = 1000000000D;
        double cost = prestige == 0 ? billion : prestige * 1.25F * billion;

        if (cost > money) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вам не хватает: $%s", cost - money);
            return false;
        }

        playerData.setMoney(money - cost);
        playerData.setPrestige(prestige + 1);

        if (i == 1)
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы прокачали престиж до: %s", StringUtils._fixDouble(0, prestige));
        return true;
    }
}
