package ru.smole.rank;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.player.OpPlayer;

public class RankManager {

    private OpPlayer opPlayer;
    private Player player;

    public RankManager(OpPlayer opPlayer) {
        this.opPlayer = opPlayer;
        player = opPlayer.getPlayer();;
    }

    public void up() {
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
        Rank rank = playerData.getRank();

        if (!isNextRank(rank)) {
            opPlayer.sendMessage("У вас максимальный ранк");
            return;
        }

        Rank nextRank = getNextRank(rank);
        double money = playerData.getMoney();
        double cost = nextRank.getCost();

        if (cost > money) {
            opPlayer.sendMessage(String.format("Вам не хватает: $%s", cost - money));
            return;
        }

        playerData.setMoney(money - cost);
        playerData.setRank(nextRank);
    }

    public Rank getNextRank(Rank rank) {
        for (int i = 1; i <= Rank.values().length; i++) {
            Rank ranks = Rank.values()[i];
            if (ranks == rank) {
                rank = Rank.values()[i + 1];
            }
        }

        return rank;
    }

    public boolean isNextRank(Rank rank) {
        return rank != Rank.Z;
    }
}
