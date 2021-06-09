package ru.smole.scoreboard;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.scoreboard.BaseScoreboardBuilder;
import ru.xfenilafs.core.scoreboard.BaseScoreboardScope;
import ru.xfenilafs.core.util.ChatUtil;

public class ScoreboardManager {
    public static void loadScoreboard(Player player) {
        BaseScoreboardBuilder scoreboardBuilder = ApiManager.newScoreboardBuilder();
        scoreboardBuilder.scoreboardDisplay("§bOpPrison");
        scoreboardBuilder.scoreboardScope(BaseScoreboardScope.PROTOTYPE);

        scoreboardBuilder.scoreboardLine(10, "");
        scoreboardBuilder.scoreboardLine(9, "§b§l" + player.getName());
        scoreboardBuilder.scoreboardLine(8, "  §fПрестижи: §bЗагрузка...");
        scoreboardBuilder.scoreboardLine(7, "  §fДобыто блоков: §bЗагрузка...");
        scoreboardBuilder.scoreboardLine(6, "");
        scoreboardBuilder.scoreboardLine(5, "§b§lБаланс");
        scoreboardBuilder.scoreboardLine(4, "  §fДеньги: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(3, "  §fТокены: §eЗагрузка...");
        scoreboardBuilder.scoreboardLine(2, "  §fМножитель: §dЗагрузка...");
        scoreboardBuilder.scoreboardLine(1, "");

        scoreboardBuilder.scoreboardUpdater(((baseScoreboard, boardPlayer) -> {
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

            baseScoreboard.updateScoreboardLine(8, boardPlayer,
                    ChatUtil.text("  §fПрестижи: §a%s", StringUtils.formatDouble(playerData.getPrestige())));

            baseScoreboard.updateScoreboardLine(7, boardPlayer,
                    ChatUtil.text("  §fДобыто блоков: §b%s", StringUtils._formatDouble(playerData.getBlocks())));

            baseScoreboard.updateScoreboardLine(4, boardPlayer,
                    ChatUtil.text("  §fДеньги: §a$%s", StringUtils.formatDouble(playerData.getMoney())));

            baseScoreboard.updateScoreboardLine(3, boardPlayer,
                    ChatUtil.text("  §fТокен: §e⛃%s", StringUtils.formatDouble(playerData.getToken())));

            baseScoreboard.updateScoreboardLine(2, boardPlayer,
                    ChatUtil.text("  §fМножитель: §d%sx", StringUtils.formatDouble(playerData.getMultiplier())));
        }), 20);

        scoreboardBuilder.build().setScoreboardToPlayer(player);
    }
}
