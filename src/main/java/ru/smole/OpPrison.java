package ru.smole;

import discord.DiscordBot;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import ru.smole.commands.*;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.player.PlayerDataManager;
import ru.smole.data.cases.Case;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.listeners.PlayerListener;
import ru.smole.listeners.RegionListener;
import ru.smole.mines.Mine;
import ru.smole.utils.server.BungeeUtil;
import ru.smole.utils.server.ServerUtil;
import ru.smole.utils.StringUtils;
import ru.smole.utils.config.ConfigManager;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.CorePlugin;
import ru.xfenilafs.core.database.RemoteDatabaseConnectionHandler;
import ru.xfenilafs.core.database.RemoteDatabasesApi;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.regions.ResourceBlock;

import java.util.*;
import java.util.stream.Collectors;

import static ru.smole.data.items.Items.registerItem;

@Slf4j
public class OpPrison extends CorePlugin {

    public @Getter
    static OpPrison instance;

    private @Getter
    PlayerDataManager playerDataManager;
    private @Getter
    ConfigManager configManager;
    private @Getter
    RemoteDatabaseConnectionHandler base;
    private @Getter
    BukkitTask pickaxeTask;
    private @Getter
    DiscordBot discordBot;
    private @Getter
    GangDataManager gangDataManager;

    public static final Map<String, Region> REGIONS = new HashMap<>();
    public static final Map<Integer, Mine> MINES = new HashMap<>();
    public static final Set<Player> BUILD_MODE = new HashSet<>();
    public static String PREFIX = "§7§l> §f";
    public static String BAR_FORMAT;
    public static BossBar BAR;
    public static double BOOSTER = 0.0;

    @SneakyThrows
    public void onPluginEnable() {
        instance = this;
        playerDataManager = new PlayerDataManager();
        gangDataManager = new GangDataManager();
        configManager = new ConfigManager();
        base = RemoteDatabasesApi.getInstance().createMysqlConnection(RemoteDatabasesApi.getInstance().createConnectionFields(
                "46.105.122.17",
                "azerusdms",
                "AGHKSF8123AIYWT1862t3iJKGHFASDqqqq",
                "OpPrison"
        ));
        discordBot = new DiscordBot("ODcwNzczMjExMzcyMDczMDgw.YQRovw.hA0SnRF-GXIGzON4AJ3cBqE2w_Y");

        BAR_FORMAT = String.format("§fБустер сервера: §b+%s §8§o(/help booster)",
                StringUtils._fixDouble(1, BOOSTER) + "%");

        BAR = Bukkit.createBossBar(BAR_FORMAT, BarColor.BLUE, BarStyle.SOLID);

        gangDataManager.load();
        ServerUtil.load();
        PickaxeManager.pickaxes = new HashMap<>();
        Items.init();

        loadCrates();
        loadRegionsAndMines();
        loadCases();
        loadLeaderBoard();
        loadEffects();

        registerListeners(
                new PlayerListener(), new RegionListener()
        );

        registerCommands(
                new MoneyCommand(), new TokenCommand(), new ItemsCommand(), new HideCommand(),
                new BuildCommand(), new StatsCommand(), new WarpCommand(), new PrestigeCommand(),
                new FlyCommand(), new HelpCommand(), new KitCommand(), new EventCommand(),
                new TrashCommand(), new DiscordCommand(null), new RestartCommand(),
                new GangCommand()
        );

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeUtil());
    }

    public void onPluginDisable() {
        Bukkit.getOnlinePlayers().forEach(playerDataManager::unload);
        base.handleDisconnect();
        BAR.removeAll();
        gangDataManager.unload();
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

    public void loadCrates() {
        ConfigurationSection section = this.getConfig().getConfigurationSection("crates");

        if (section != null && section.getKeys(false).size() > 0) {
            section.getKeys(false).forEach((key) -> {
                ConfigurationSection keySection = section.getConfigurationSection(key);
                new Crate(key, keySection);
            });
        }

        Arrays.stream(Crate.Type.values())
                .forEach(crate ->
                        registerItem(
                                String.format("%s_crate", crate.name().toLowerCase()),
                                objects -> ApiManager.newItemBuilder(crate.getStack()).setAmount(((Double) objects[0]).intValue()).build(),
                                (playerInteractEvent, itemStack) -> {
                                    Action action = playerInteractEvent.getAction();

                                    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                                        ItemStack item = playerInteractEvent.getItem();

                                        if (Crate.crates.containsKey(crate.name().toLowerCase())) {
                                            item.setAmount(item.getAmount() - 1);
                                            Crate.crates.get(crate.name().toLowerCase()).open(playerInteractEvent.getPlayer());
                                        }
                                    }
                                })
                );
    }

    private void loadLeaderBoard() {
        LeaderBoard blocks = new LeaderBoard(
                "&bТоп по блокам",
                new Location(Bukkit.getWorld("world"), 34, 122, 2),
                "blocks"
        );

        LeaderBoard prestige = new LeaderBoard(
                "&bТоп по престижам",
                new Location(Bukkit.getWorld("world"), 23, 122, 2),
                "prestige"
        );

        SimpleHolographic simpleHolographic = new SimpleHolographic(new Location(Bukkit.getWorld("world"), 28.5, 121.5, 1));

        simpleHolographic.addTextLine("§fДобро пожаловать на §bOpPrison§f!");
        simpleHolographic.addEmptyLine();
        simpleHolographic.addTextLine("§fВсю полезную информация Вы можете узнать через §b/help");
        simpleHolographic.addTextLine("§fВаша основная цель - прокачать свою кирку как можно лучше");
        simpleHolographic.addEmptyLine();
        simpleHolographic.addTextLine("§fЖелаем удачи Вам в ваших начинаниях!");

        LeaderBoard.holograms.add(simpleHolographic);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (Bukkit.getOnlinePlayers().size() == 0)
                return;

            Bukkit.getOnlinePlayers().forEach(getPlayerDataManager()::updateTop);

            blocks.update();
            prestige.update();
        }, 20 * 300, 20 * 300);
    }

    private void loadEffects() {
        pickaxeTask = Bukkit.getScheduler().runTaskTimer(this, () ->
                Bukkit.getOnlinePlayers().forEach(player -> {
                    String item = Items.getItemName(player.getInventory().getItemInMainHand());

                    if (item.length() < 1 || !item.equals("pickaxe"))
                        return;

                    Pickaxe pickaxe = PickaxeManager.pickaxes.get(player.getName());
                    val upgrades = pickaxe.getUpgrades();

                    int hasteLevel = (int) upgrades.get(Upgrade.HASTE).getCount();
                    int speedLevel = (int) upgrades.get(Upgrade.SPEED).getCount();
                    int jump_boostLevel = (int) upgrades.get(Upgrade.JUMP_BOOST).getCount();
                    int night_visionLevel = (int) upgrades.get(Upgrade.NIGHT_VISION).getCount();

                    if (hasteLevel != 0) {
                        if (upgrades.get(Upgrade.HASTE).isIs()) {
                            if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING))
                                player.removePotionEffect(PotionEffectType.FAST_DIGGING);

                            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, hasteLevel));
                        }
                    }

                    if (speedLevel != 0) {
                        if (upgrades.get(Upgrade.SPEED).isIs()) {
                            if (player.hasPotionEffect(PotionEffectType.SPEED))
                                player.removePotionEffect(PotionEffectType.SPEED);

                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, speedLevel));
                        }
                    }

                    if (jump_boostLevel != 0) {
                        if (upgrades.get(Upgrade.JUMP_BOOST).isIs()) {
                            if (player.hasPotionEffect(PotionEffectType.JUMP))
                                player.removePotionEffect(PotionEffectType.JUMP);

                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 80, jump_boostLevel));
                        }
                    }

                    if (night_visionLevel != 0) {
                        if (upgrades.get(Upgrade.NIGHT_VISION).isIs()) {
                            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                                player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, night_visionLevel));
                        }
                    }
                }), 20, 20);
    }
}
