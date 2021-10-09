package ru.smole;

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
import ru.smole.data.cases.Case;
import ru.smole.data.event.OpEvent;
import ru.smole.data.event.OpEvents;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.npc.NpcInitializer;
import ru.smole.data.npc.question.Question;
import ru.smole.data.pads.LaunchPad;
import ru.smole.data.player.PlayerData;
import ru.smole.data.player.PlayerDataManager;
import ru.smole.listeners.PlayerListener;
import ru.smole.listeners.RegionListener;
import ru.smole.mines.Mine;
import ru.smole.utils.StringUtils;
import ru.smole.utils.WorldBorderUtils;
import ru.smole.utils.config.ConfigManager;
import ru.smole.utils.config.ConfigUtils;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.smole.utils.server.BungeeUtil;
import ru.smole.utils.server.ServerUtil;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.CorePlugin;
import ru.xfenilafs.core.database.RemoteDatabaseConnectionHandler;
import ru.xfenilafs.core.database.RemoteDatabaseTable;
import ru.xfenilafs.core.database.RemoteDatabasesApi;
import ru.xfenilafs.core.database.query.row.TypedQueryRow;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.regions.ResourceBlock;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.smole.data.items.Items.registerItem;
import static ru.xfenilafs.core.database.query.RemoteDatabaseRowType.*;

@Slf4j
public class OpPrison extends CorePlugin {

    public @Getter static OpPrison instance;

    private @Getter
    PlayerDataManager playerDataManager;
    private @Getter
    ConfigManager configManager;
    private @Getter
    RemoteDatabaseConnectionHandler base;
    private @Getter
    GangDataManager gangDataManager;
    private @Getter
    RemoteDatabaseTable players;
    private @Getter
    RemoteDatabaseTable gangs;

    public static final Map<String, Region> REGIONS = new HashMap<>();
    public static final Map<Integer, Mine> MINES = new HashMap<>();
    public static final Set<Player> BUILD_MODE = new HashSet<>();
    public static final List<LaunchPad> PADS = new ArrayList<>();
    public static String PREFIX = "§f";
    public static String BAR_FORMAT;
    public static BossBar BAR;
    public static double BOOSTER = 0.0;

    private BukkitTask pickaxeTask;
    private BukkitTask eventTask;

    @SneakyThrows
    public void onPluginEnable() {
        instance = this;
        playerDataManager = new PlayerDataManager();
        gangDataManager = new GangDataManager();
        configManager = new ConfigManager();
        base = RemoteDatabasesApi.getInstance().createMysqlConnection(RemoteDatabasesApi.getInstance().createConnectionFields(
                "localhost",
                "root",
                "OZ|NF6za38YMWSOV6PJMXR$Xv96WqlG*hDL4Ev{98{@d6q*|LN",
                "OpPrison"
        ));

        base.newDatabaseQuery("players").createTableQuery().setCanCheckExists(true)
                .queryRow(new TypedQueryRow(TEXT,"name"))
                .queryRow(new TypedQueryRow(DOUBLE,"blocks"))
                .queryRow(new TypedQueryRow(DOUBLE,"money"))
                .queryRow(new TypedQueryRow(DOUBLE,"token"))
                .queryRow(new TypedQueryRow(DOUBLE,"multiplier"))
                .queryRow(new TypedQueryRow(DOUBLE,"prestige"))
                .queryRow(new TypedQueryRow(TEXT,"rank"))
                .queryRow(new TypedQueryRow(INT,"fly"))
                .queryRow(new TypedQueryRow(TEXT,"pickaxe"))
                .queryRow(new TypedQueryRow(TEXT,"kit"))
                .queryRow(new TypedQueryRow(TEXT,"access"))
                .queryRow(new TypedQueryRow(TEXT,"questions"))
                .executeSync(base);

        base.newDatabaseQuery("gangs").createTableQuery().setCanCheckExists(true)
                .queryRow(new TypedQueryRow(TEXT, "name"))
                .queryRow(new TypedQueryRow(TEXT, "members"))
                .queryRow(new TypedQueryRow(DOUBLE, "score"))
                .executeSync(base);

        players = base.getTable("players");
        gangs = base.getTable("gangs");

        BAR_FORMAT = String.format("§fБустер сервера: §b+%s §8§o(/help booster)",
                StringUtils._fixDouble(1, BOOSTER) + "%");

        BAR = Bukkit.createBossBar(BAR_FORMAT, BarColor.BLUE, BarStyle.SOLID);

        registerListeners(
                new PlayerListener(), new RegionListener()
        );

        registerCommands(
                new MoneyCommand(), new TokenCommand(), new ItemsCommand(), new HideCommand(),
                new BuildCommand(), new StatsCommand(), new WarpCommand(), new PrestigeCommand(),
                new FlyCommand(), new HelpCommand(), new KitCommand(), new EventCommand(),
                new TrashCommand(), new RestartCommand(), new GangCommand(), new GangChatCommand(),
                new SpawnCommand(), new EnderChestCommand()
        );

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeUtil());

        gangDataManager.load();
        ServerUtil.load();
        PickaxeManager.pickaxes = new HashMap<>();
        Items.init();

        loadCrates();
        loadRegionsAndMines();
        loadCases();
        loadLeaderBoard();
        loadEffects();
        loadEvents();

        NpcInitializer.init();
    }

    public void onPluginDisable() {
        Bukkit.getOnlinePlayers().forEach(playerDataManager::unload);
        base.handleDisconnect();
        BAR.removeAll();
        gangDataManager.unload();
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

            WorldBorderUtils.spawn(region.getZone().getWorld(), region.getZone().getCenter(), 250);
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

                        return
                                new ResourceBlock(
                                    Material.valueOf(!split[0].contains(":") ? split[0] : split[0].split(":")[0]),
                                    Integer.parseInt(split[1]),
                                        !split[0].contains(":") ? 0 : Integer.parseInt(split[0].split(":")[1])
                                );
                    })
                            .collect(Collectors.toList())
            );
            MINES.put(mine.getLevel(), mine);

        });
        log.info("Loaded {} mines!", MINES.size());

        Bukkit.getScheduler().runTaskTimer(this, () -> MINES.values().forEach(Mine::reset), 20L, 20L);

        FileConfiguration misc = configManager.getMiscConfig().getConfiguration();
        ConfigurationSection pads = misc.getConfigurationSection("pads");
        pads.getKeys(false).forEach(s ->
                PADS.add(new LaunchPad(pads.getConfigurationSection(s)))
        );
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

    private void loadLeaderBoard() {
        FileConfiguration miscConfig = configManager.getMiscConfig().getConfiguration();
        LeaderBoard blocks = new LeaderBoard(
                "&bТоп по блокам",
                ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("tops-block")),
                "blocks"
        );

        LeaderBoard prestige = new LeaderBoard(
                "&bТоп по престижам",
                ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("tops-prestige")),
                "prestige"
        );

        SimpleHolographic simpleHolographic = new SimpleHolographic(ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("info")));

        simpleHolographic.addTextLine("§fДобро пожаловать на §bOpPrison§f!");
        simpleHolographic.addEmptyLine();
        simpleHolographic.addTextLine("§fВаша первая шахта §7> §f/warp §7> §fШахты для групп §7> §7MANTLE §fшахта");
        simpleHolographic.addTextLine("§fНа данном режиме цель является прокачать свою кирку и престиж §8§o(/prestige|p max)");
        simpleHolographic.addTextLine("§fА остальную информацию Вы можете узнать через §b/help");
        simpleHolographic.addEmptyLine();
        simpleHolographic.addTextLine("§fЖелаем удачи Вам в ваших начинаниях!");
        LeaderBoard.holograms.add(simpleHolographic);

        SimpleHolographic caseHere = new SimpleHolographic(ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("caseHere")));

        caseHere.addTextLine("   §b^^^   ");
        caseHere.addTextLine("§fКейсы");

        LeaderBoard.holograms.add(caseHere);

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

    private void loadEvents() {
        OpEvent opEvent = new OpEvents();
        Map<String, Double> blocks = new HashMap<>();
        var events = opEvent.getBreakEvents();

        String name = "sofos";
        events.put(name, event -> {
            Player player = event.getPlayer();
            String playerName = player.getName();

            if (MINES.get(-1).getBlocks().stream().noneMatch(resourceBlock -> resourceBlock.getType() == event.getBlock().getType()))
                return;

            blocks.remove(playerName);
            blocks.put(playerName, blocks.get(playerName) + 1);
        });

        eventTask =
                Bukkit.getScheduler().runTaskTimer(
                        this,
                        () -> {
                            if (Bukkit.getOnlinePlayers().size() < 5) {
                                return;
                            }

                            Predicate<Player> predicate = player -> getPlayerDataManager().getPlayerDataMap().get(player.getName()).getPrestige() >= 1500000;

                            if (Bukkit.getOnlinePlayers().stream().noneMatch(predicate))
                                return;

                            ChatUtil.broadcast(PREFIX + "&fСобытие &bСостязания &fначалось");
                            ChatUtil.broadcast("");
                            ChatUtil.broadcast("   Суть события в том, чтобы телепортироваться на шахту 150.000.000 &8&o(150M) &fпрестижей");
                            ChatUtil.broadcast("   и накопать больше всех блоков за 20 минут");
                            ChatUtil.broadcast("");

                            Bukkit.getOnlinePlayers().forEach(player -> blocks.put(player.getName(), 0.0));
                            opEvent.start(name);

                            Bukkit.getScheduler().runTaskLater(
                                    this,
                                    () -> {
                                        ChatUtil.broadcast(PREFIX + "&fСобытие &bСостязания &fзавершилось");
                                        ChatUtil.broadcast("");

                                        int[] i = {1};

                                        blocks.entrySet()
                                                .stream()
                                                .sorted(Map.Entry.comparingByValue())
                                                .limit(3)
                                                .forEachOrdered(x -> {
                                                    ChatUtil.broadcast("   &7%s. &b%s &f- &b%s", i[0], x.getKey(), StringUtils.replaceComma(x.getValue()));

                                                    PlayerData playerData = getPlayerDataManager().getPlayerDataMap().get(x.getKey());
                                                    double added = playerData.getToken() * 0.15 / i[0];

                                                    playerData.addToken(added);
                                                    i[0]++;

                                                    Question question = playerData.getQuestions().get("SOFOS");
                                                    Question.QuestionStep step = question.getStep();

                                                    if (step == Question.QuestionStep.COMPLETING) {
                                                        question.setStep(Question.QuestionStep.ALR_COMPLETED);
                                                    }
                                                });

                                        ChatUtil.broadcast("");


                                        blocks.clear();
                                        opEvent.stop(name);
                                    },
                                    20 * 60 * 20
                            );
                        },
                        20 * 60 * 40,
                        20 * 60 * 40
                        );
    }
}
