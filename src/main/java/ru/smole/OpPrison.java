package ru.smole;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.smole.cases.Case;
import ru.smole.commands.*;
import ru.smole.data.PlayerDataManager;
import ru.smole.data.mysql.DatabaseManager;
import ru.smole.listeners.PlayerListener;
import ru.smole.listeners.RegionListener;
import ru.smole.mines.Mine;
import ru.smole.utils.config.ConfigManager;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventoryListener;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.regions.ResourceBlock;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class OpPrison extends JavaPlugin {

    public @Getter static OpPrison instance;

    private @Getter PlayerDataManager playerDataManager;
    private @Getter ConfigManager configManager;
    private @Getter DatabaseManager base;

    public static final Map<String, Region> REGIONS = new HashMap<>();
    public static final Map<Integer, Mine> MINES = new HashMap<>();
    public static final Set<Player> BUILD_MODE = new HashSet<>();
    public static String PREFIX = "&bOpPrison &7>> &f";

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager();
        playerDataManager = new PlayerDataManager();
        base = new DatabaseManager(
                "localhost",
                "OpPrison",
                "root",
                "vi6RcaDhRvkO0U5d",
                false
        );

        ApiManager.registerListeners(this,
                new PlayerListener(), new RegionListener(), new BaseInventoryListener()
        );

        ApiManager.registerCommands(
                new MoneyCommand(), new TokenCommand(), new ItemsCommand(), new HideCommand(),
                new BuildCommand(), new RankUpCommand(), new StatsCommand(), new WarpCommand()
        );
        
        loadRegionsAndMines();
        loadCases();
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> playerDataManager.unload(player));
        base.close();
    }

    private void loadRegionsAndMines() {
        FileConfiguration config = configManager.getRegionConfig().getConfiguration();
        ConfigurationSection regions = config.getConfigurationSection("regions");
        regions.getKeys(false).forEach(key -> {
            ConfigurationSection section = regions.getConfigurationSection(key);
            String name = section.getString("name");
            boolean pvp = section.getBoolean("pvp");
            String[] spawnLocText = section.getString("spawnLoc").split(" ");
            String[] maxPoint = section.getString("maxPoint").split(" ");
            String[] minPoint = section.getString("minPoint").split(" ");
            Location spawnLoc = new Location(
                    Bukkit.getWorld(spawnLocText[0]),
                    Double.parseDouble(spawnLocText[1]),
                    Double.parseDouble(spawnLocText[2]),
                    Double.parseDouble(spawnLocText[3])
            );
            if (spawnLocText.length >= 6) {
                spawnLoc.setYaw(Float.parseFloat(spawnLocText[4]));
                spawnLoc.setPitch(Float.parseFloat(spawnLocText[5]));
            }
            Region region = new Region(
                    name,
                    pvp,
                    spawnLoc,
                    new Location(
                            Bukkit.getWorld(maxPoint[0]),
                            Double.parseDouble(maxPoint[1]),
                            Double.parseDouble(maxPoint[2]),
                            Double.parseDouble(maxPoint[3])
                    ),
                    new Location(
                            Bukkit.getWorld(minPoint[0]),
                            Double.parseDouble(minPoint[1]),
                            Double.parseDouble(minPoint[2]),
                            Double.parseDouble(minPoint[3])
                    )
            );
            REGIONS.put(name.toLowerCase(), region);
        });
        log.info("Loaded {} regions!", REGIONS.size());

        ConfigurationSection mines = config.getConfigurationSection("mines");
        mines.getKeys(false).forEach(key -> {
            ConfigurationSection section = mines.getConfigurationSection(key);
            int level = section.getInt("level");
            String region = section.getString("region");
            String world = section.getString("world");
            String maxPoint = section.getString("maxPoint");
            String minPoint = section.getString("minPoint");
            String resetTime = section.getString("resetTime");
            List<String> blocks = section.getStringList("blocks");
            Mine mine = new Mine(
                    level,
                    region,
                    world,
                    minPoint,
                    maxPoint,
                    resetTime,
                    blocks.stream().map(b -> {
                        String[] split = b.split(" ");
                        return new ResourceBlock(Material.valueOf(split[0].toUpperCase()), Integer.parseInt(split[1]));
                    })
                            .collect(Collectors.toList())
            );
            MINES.put(mine.getLevel(), mine);
        });
        log.info("Loaded {} mines!", MINES.size());

        Bukkit.getScheduler().runTaskTimer(this, () -> MINES.values().forEach(Mine::reset), 20L, 20L);
    }

    private void loadCases() {
        ConfigurationSection section = this.getConfig().getConfigurationSection("cases");
        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((key) -> {
                ConfigurationSection keySection = section.getConfigurationSection(key);
                new Case(key, keySection);
            });
        }

    }
}
