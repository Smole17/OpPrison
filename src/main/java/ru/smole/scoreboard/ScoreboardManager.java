package ru.smole.scoreboard;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.scoreboard.BaseScoreboardBuilder;
import ru.xfenilafs.core.scoreboard.BaseScoreboardScope;
import ru.xfenilafs.core.util.ChatUtil;

public class ScoreboardManager {
    public static void loadScoreboard(Player player) {
        BaseScoreboardBuilder scoreboardBuilder = ApiManager.newScoreboardBuilder();
        scoreboardBuilder.scoreboardDisplay("§b§lＯＰＰＲＩＳＯＮ");
        scoreboardBuilder.scoreboardScope(BaseScoreboardScope.PROTOTYPE);

        scoreboardBuilder.scoreboardLine(12, "");
        scoreboardBuilder.scoreboardLine(11, "§b" + player.getName());
        scoreboardBuilder.scoreboardLine(10, "  §fДобыто блоков: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(9, "  §fБанда: Загрузка...");
        scoreboardBuilder.scoreboardLine(8, "  §fПрестиж: §bЗагрузка...");
        scoreboardBuilder.scoreboardLine(7, "  §fГруппа: §fЗагрузка...");
        scoreboardBuilder.scoreboardLine(6, "");
        scoreboardBuilder.scoreboardLine(5, "§bБаланс");
        scoreboardBuilder.scoreboardLine(4, "  §fДеньги: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(3, "  §fТокены: §eЗагрузка...");
        scoreboardBuilder.scoreboardLine(2, "  §fМножитель: §dЗагрузка...");
        scoreboardBuilder.scoreboardLine(1, "");

        scoreboardBuilder.scoreboardUpdater(((baseScoreboard, boardPlayer) -> {
            String playerName = boardPlayer.getName();
            OpPrison main = OpPrison.getInstance();

            PlayerData playerData = main.getPlayerDataManager().getPlayerDataMap().get(playerName);
            GangDataManager gManager = main.getGangDataManager();

            baseScoreboard.updateScoreboardLine(9, boardPlayer,
                    ChatUtil.text("  §fБанда: %s", gManager.playerHasGang(playerName) ? gManager.getGangFromPlayer(playerName).getName() : "&c-"));

            baseScoreboard.updateScoreboardLine(8, boardPlayer,
                    ChatUtil.text("  §fПрестиж: §a%s", StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getPrestige()).length() <= 3 ? 0 : 2, playerData.getPrestige())));

            baseScoreboard.updateScoreboardLine(7, boardPlayer,
                    ChatUtil.text("  §fГруппа: §f%s", playerData.getGroup().getName()));

            baseScoreboard.updateScoreboardLine(10, boardPlayer,
                    ChatUtil.text("  §fДобыто блоков: §a%s", StringUtils._fixDouble(0, playerData.getBlocks())));

            baseScoreboard.updateScoreboardLine(4, boardPlayer,
                    ChatUtil.text("  §fДеньги: §a$%s", StringUtils.formatDouble(2, playerData.getMoney())));

            baseScoreboard.updateScoreboardLine(3, boardPlayer,
                    ChatUtil.text("  §fТокены: §e⛃%s", StringUtils.formatDouble(2, playerData.getToken())));

            baseScoreboard.updateScoreboardLine(2, boardPlayer,
                    ChatUtil.text("  §fМножитель: §d%sx", StringUtils._fixDouble(0, playerData.getMultiplier())));
        }), 30);

        scoreboardBuilder.build().setScoreboardToPlayer(player);
    }
}
