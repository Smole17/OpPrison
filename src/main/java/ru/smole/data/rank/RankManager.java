package ru.smole.data.rank;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.xfenilafs.core.util.ChatUtil;

public class RankManager {

    private Player player;
    private PlayerData playerData;

    public RankManager(Player player) {
        this.player = player;
        playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
    }

    public void up() {
        Rank rank = playerData.getRank();

        if (!isNextRank(rank)) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "У вас максимальный ранк");
            return;
        }

        Rank nextRank = getNextRank(rank);
        double money = playerData.getMoney();
        double cost = nextRank.getCost();

        if (cost > money) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вам не хватает: $%s", cost - money);
            return;
        }

        playerData.setMoney(money - cost);
        playerData.setRank(nextRank);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы прокачали свой ранк до %s", nextRank.getName());
    }

    public Rank getNextRank(Rank rank) {
        for (int i = 1; i <= Rank.values().length; i++) {
            Rank ranks = Rank.values()[i -1];
            if (ranks == rank) {
                return Rank.values()[i];
            }
        }

        return rank;
    }

    public boolean isNextRank(Rank rank) {
        return rank != Rank.Z;
    }

    public Rank getRankFromString(String rank) {
        for (Rank ranks : Rank.values()) {
            if (ranks == Rank.valueOf(rank)) {
                return ranks;
            }
        }

        return null;
    }

    public boolean isEquals(Rank rank) {
        return playerData.getRank().getPriority() <= rank.getPriority();
    }
}
