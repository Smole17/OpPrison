package ru.smole.scoreboard;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
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

        scoreboardBuilder.scoreboardLine(11, "");
        scoreboardBuilder.scoreboardLine(10, "§b§l" + player.getName());
        scoreboardBuilder.scoreboardLine(9, "  §fПрестижи: §bЗагрузка...");
        scoreboardBuilder.scoreboardLine(8, "  §fДобыто блоков: §bЗагрузка...");
        scoreboardBuilder.scoreboardLine(7, "  §fГруппа: §fЗагрузка...");
        scoreboardBuilder.scoreboardLine(6, "");
        scoreboardBuilder.scoreboardLine(5, "§b§lБаланс");
        scoreboardBuilder.scoreboardLine(4, "  §fДеньги: §aЗагрузка...");
        scoreboardBuilder.scoreboardLine(3, "  §fТокены: §eЗагрузка...");
        scoreboardBuilder.scoreboardLine(2, "  §fМножитель: §dЗагрузка...");
        scoreboardBuilder.scoreboardLine(1, "");

        scoreboardBuilder.scoreboardUpdater(((baseScoreboard, boardPlayer) -> {
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());

            baseScoreboard.updateScoreboardLine(9, boardPlayer,
                    ChatUtil.text("  §fПрестижи: §a%s", StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getPrestige()).length() <= 3 ? 0 : 2, playerData.getPrestige())));

            baseScoreboard.updateScoreboardLine(7, boardPlayer,
                    ChatUtil.text("  §fГруппа: §f%s", playerData.getGroup().getName()));

            baseScoreboard.updateScoreboardLine(8, boardPlayer,
                    ChatUtil.text("  §fДобыто блоков: §b%s", StringUtils._fixDouble(0, playerData.getBlocks())));

            baseScoreboard.updateScoreboardLine(4, boardPlayer,
                    ChatUtil.text("  §fДеньги: §a$%s", StringUtils.formatDouble(2, playerData.getMoney())));

            baseScoreboard.updateScoreboardLine(3, boardPlayer,
                    ChatUtil.text("  §fТокены: §e⛃%s", StringUtils.formatDouble(2, playerData.getToken())));

            baseScoreboard.updateScoreboardLine(2, boardPlayer,
                    ChatUtil.text("  §fМножитель: §d%sx", StringUtils._fixDouble(0, playerData.getMultiplier())));
        }), 20);

        scoreboardBuilder.build().setScoreboardToPlayer(player);
    }
}
