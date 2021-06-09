
package ru.smole;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.smole.commands.*;
import ru.smole.data.PlayerDataManager;
import ru.smole.data.mysql.DatabaseManager;
import ru.smole.listeners.ChatHandler;
import ru.smole.listeners.PlayerHandler;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;

public final class OpPrison extends JavaPlugin {

    public @Getter static OpPrison instance;

    private @Getter PlayerDataManager playerDataManager;
    private @Getter ConfigManager configManager;
    private @Getter DatabaseManager base;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager();
        playerDataManager = new PlayerDataManager();
        base = new DatabaseManager("localhost", "OpPrison", "root", "vi6RcaDhRvkO0U5d", false);

        ApiManager.registerListeners(this, new PlayerHandler(), new ChatHandler());
        ApiManager.registerCommands(
                new MoneyCommand(),
                new TokenCommand(),
                new ItemsCommand(),
                new HideCommand(),
                new RankUpCommand());
        createDatabase();
    }

    @Override
    public void onDisable() {
        base.close();
    }

    public void createDatabase() {
        base.update("CREATE TABLE IF NOT EXISTS " +
                "OpPrison(" +
                "name VARCHAR(16), " +
                "blocks DOUBLE, " +
                "money DOUBLE, " +
                "token DOUBLE, " +
                "multiplier DOUBLE, " +
                "prestige DOUBLE, " +
                "rank VARCHAR(1))");
    }
}
