package ru.smole.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.scoreboard.BaseScoreboard;
import ru.xfenilafs.core.scoreboard.BaseScoreboardBuilder;
import ru.xfenilafs.core.scoreboard.BaseScoreboardScope;
import ru.xfenilafs.core.scoreboard.ScoreboardUpdater;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.Schedules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScoreboardManager {

    private final @Getter BaseScoreboardBuilder scoreboardBuilder;
    private final @Getter List<ScoreboardUpdater> updaters;

    private @Getter BaseScoreboard baseScoreboard;

    private ScoreboardManager(String scoreboardName, BaseScoreboardScope scoreboardScope) {
        scoreboardBuilder = ApiManager.newScoreboardBuilder()
                .scoreboardDisplay(scoreboardName)
                .scoreboardScope(scoreboardScope);
        this.updaters = new ArrayList<>();
    }

    public static ScoreboardManager get(String scoreboardName, BaseScoreboardScope scoreboardScope) {
        return new ScoreboardManager(scoreboardName, scoreboardScope);
    }

    public ScoreboardManager line(int i, String put) {
        removeLine(i);
        scoreboardBuilder.scoreboardLine(i, put);
        return this;
    }

    public ScoreboardManager updateLine(int i, Player player, String put) {
        if (baseScoreboard == null)
            return this;

        baseScoreboard.updateScoreboardLine(i, player, put);
        return this;
    }

    public ScoreboardManager removeLine(int i) {
        scoreboardBuilder.getScoreboardLineMap().remove(i);
        return this;
    }

    public ScoreboardManager updater(ru.xfenilafs.core.scoreboard.ScoreboardUpdater updater, int tick) {
        updaters.add(new ScoreboardUpdater(updater, tick));
        return this;
    }

    public ScoreboardManager build() {
        baseScoreboard = scoreboardBuilder.build();

        if (!updaters.isEmpty())
            updaters.forEach(scoreboardUpdater -> baseScoreboard.addScoreboardUpdater(scoreboardUpdater.getUpdater(), scoreboardUpdater.getTick()));

        reloadScoreboard();
        return this;
    }

    public void loadScoreboard(Player player) {
        baseScoreboard.setScoreboardToPlayer(player);
    }

    public void unloadScoreboard(Player player) {
        baseScoreboard.removeScoreboardToPlayer(player);
    }

    public void reloadScoreboard() {
        if (Bukkit.getOnlinePlayers().isEmpty())
            return;

        Bukkit.getOnlinePlayers()
                .stream()
                .parallel()
                .forEach(player -> {
                    unloadScoreboard(player);
                    loadScoreboard(player);
                });
    }

    @AllArgsConstructor
    @Data
    public static class ScoreboardUpdater {

        private ru.xfenilafs.core.scoreboard.ScoreboardUpdater updater;
        private int tick;
    }
}
