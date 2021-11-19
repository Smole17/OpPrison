package ru.smole;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.spigotmc.AsyncCatcher;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.commands.*;
import ru.smole.data.booster.BoosterManager;
import ru.smole.data.cases.Case;
import ru.smole.data.event.EventManager;
import ru.smole.data.event.impl.BlockEvent;
import ru.smole.data.event.impl.PointEvent;
import ru.smole.data.gang.GangDataManager;
import ru.smole.data.items.Items;
import ru.smole.data.items.crates.Crate;
import ru.smole.data.items.pickaxe.Pickaxe;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.mysql.GangDataSQL;
import ru.smole.data.npc.NpcInitializer;
import ru.smole.data.npc.question.Question;
import ru.smole.data.pads.LaunchPad;
import ru.smole.data.player.PlayerData;
import ru.smole.data.player.PlayerDataManager;
import ru.smole.data.pvpcd.PvPCooldown;
import ru.smole.listeners.PlayerListener;
import ru.smole.listeners.RegionListener;
import ru.smole.mines.Mine;
import ru.smole.scoreboard.ScoreboardManager;
import ru.smole.utils.StringUtils;
import ru.smole.utils.WorldBorderUtils;
import ru.smole.utils.config.ConfigManager;
import ru.smole.utils.config.ConfigUtils;
import ru.smole.utils.leaderboard.LeaderBoard;
import ru.smole.utils.server.ServerUtil;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.CorePlugin;
import ru.xfenilafs.core.database.RemoteDatabaseConnectionHandler;
import ru.xfenilafs.core.database.RemoteDatabaseTable;
import ru.xfenilafs.core.database.RemoteDatabasesApi;
import ru.xfenilafs.core.database.query.row.TypedQueryRow;
import ru.xfenilafs.core.holographic.impl.SimpleHolographic;
import ru.xfenilafs.core.player.world.TypeStats;
import ru.xfenilafs.core.player.world.WorldStatistic;
import ru.xfenilafs.core.regions.Region;
import ru.xfenilafs.core.regions.ResourceBlock;
import ru.xfenilafs.core.scoreboard.BaseScoreboardScope;
import ru.xfenilafs.core.util.ChatUtil;
import ru.xfenilafs.core.util.Schedules;
import ru.xfenilafs.core.util.cuboid.Cuboid;
import sexy.kostya.mineos.achievements.Achievement;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.smole.data.items.Items.registerItem;
import static ru.xfenilafs.core.database.query.RemoteDatabaseRowType.*;

@Slf4j
public class OpPrison extends CorePlugin {

    private @Getter static OpPrison instance;

    private @Getter PlayerDataManager playerDataManager;
    private @Getter ConfigManager configManager;
    private @Getter RemoteDatabaseConnectionHandler base;
    private @Getter GangDataManager gangDataManager;
    private @Getter RemoteDatabaseTable players;
    private @Getter RemoteDatabaseTable gangs;
    private @Getter WorldStatistic worldStatistic;
    private @Getter PvPCooldown pvPCooldown;
    private @Getter EventManager eventManager;
    private @Getter ScoreboardManager scoreboardManager;

    public static final Map<String, Region> REGIONS = new HashMap<>();
    public static final Map<Integer, Mine> MINES = new HashMap<>();
    public static final Set<Player> BUILD_MODE = new HashSet<>();
    public static final List<LaunchPad> PADS = new ArrayList<>();
    public static String PREFIX = "§a";
    public static String PREFIX_N = "§c";
    public static BossBar BAR;
    public static double BOOSTER = 0.0;

    @SneakyThrows
    public void onPluginEnable() {
        instance = this;
        playerDataManager = new PlayerDataManager();
        gangDataManager = new GangDataManager();
        configManager = new ConfigManager();
        base = RemoteDatabasesApi.getInstance().createMysqlConnection(RemoteDatabasesApi.getInstance().createConnectionFields(
                "162.55.103.53",
                "root",
                "DevmysqlHBJFhjFHAJAFHJHKy1123fHJ%qYTUKLJHASdASDHJhjf",
                "OpPrison"
        ));

        base.newDatabaseQuery("players").createTableQuery().setCanCheckExists(true)
                .queryRow(new TypedQueryRow(TEXT, "name"))
                .queryRow(new TypedQueryRow(DOUBLE, "blocks"))
                .queryRow(new TypedQueryRow(DOUBLE, "money"))
                .queryRow(new TypedQueryRow(DOUBLE, "token"))
                .queryRow(new TypedQueryRow(DOUBLE, "multiplier"))
                .queryRow(new TypedQueryRow(DOUBLE, "prestige"))
                .queryRow(new TypedQueryRow(TEXT, "rank"))
                .queryRow(new TypedQueryRow(INT, "fly"))
                .queryRow(new TypedQueryRow(TEXT, "pickaxe"))
                .queryRow(new TypedQueryRow(TEXT, "kit"))
                .queryRow(new TypedQueryRow(TEXT, "access"))
                .queryRow(new TypedQueryRow(TEXT, "questions"))
                .queryRow(new TypedQueryRow(TEXT, "battlepass"))
                .executeAsync(base);

        base.newDatabaseQuery("gangs").createTableQuery().setCanCheckExists(true)
                .queryRow(new TypedQueryRow(TEXT, "name"))
                .queryRow(new TypedQueryRow(TEXT, "members"))
                .queryRow(new TypedQueryRow(DOUBLE, "score"))
                .queryRow(new TypedQueryRow(TEXT, "vault"))
                .executeAsync(base);

        players = base.getTable("players");
        gangs = base.getTable("gangs");

        BAR = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);

        pvPCooldown = new PvPCooldown();
        eventManager = new EventManager();
        scoreboardManager = ScoreboardManager.get("§b§lＯＰＰＲＩＳＯＮ", BaseScoreboardScope.PROTOTYPE);

        registerListeners(
                new PlayerListener(), new RegionListener()
        );

        registerCommands(
                new MoneyCommand(), new TokenCommand(), new ItemsCommand(), new BuildCommand(),
                new StatsCommand(), new WarpCommand(), new PrestigeCommand(), new FlyCommand(),
                new InfoCommand(), new KitCommand(), new EventCommand(), new TrashCommand(),
                new RestartCommand(), new GangCommand(), new GangChatCommand(), new SpawnCommand(),
                new EnderChestCommand(), new MineCommand(), new RepairCommand(), new GangSetCommand(),
                new MineCommand()
        );

        gangDataManager.load();
        ServerUtil.load();
        PickaxeManager.pickaxes = new HashMap<>();
        Items.init();
        worldStatistic = WorldStatistic.init(this, base, TypeStats.values());

        loadScoreboard();
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

    public void loadScoreboard() {
        scoreboardManager.line(10, "")
                .line(9, "§7Загрузка...")
                .line(8, "")
                .line(7, "§7Загрузка...")
                .line(6, "§7Загрузка...")
                .line(5, "")
                .updater(((baseScoreboard, boardPlayer) -> {
                    String playerName = boardPlayer.getName();

                    PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(playerName);

                    baseScoreboard.updateScoreboardLine(9, boardPlayer,
                            ChatUtil.text(
                                    "§a❖ §fПрестиж: §a%s",
                                    StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getPrestige()).length() <= 3 ? 0 : 2, playerData.getPrestige())
                            )
                    );

                    baseScoreboard.updateScoreboardLine(7, boardPlayer,
                            ChatUtil.text(
                                    "§fДеньги: §6$%s",
                                    StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getMoney()).length() <= 3 ? 0 : 2, playerData.getMoney())
                            )
                    );

                    baseScoreboard.updateScoreboardLine(6, boardPlayer,
                            ChatUtil.text(
                                    "§fТокены: §e⛃%s",
                                    StringUtils.formatDouble(StringUtils._fixDouble(0, playerData.getToken()).length() <= 3 ? 0 : 2, playerData.getToken())
                            )
                    );
                }), 5)
                .build();
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

            Cuboid cuboid = region.getZone();

            if (!cuboid.getWorld().getName().contains("gangs"))
                WorldBorderUtils.spawn(region.getZone().getWorld(), cuboid.getCenter(), cuboid.getSizeX() + cuboid.getSizeZ());

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
            double bonus = section.getDouble("bonus");
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
                    }).collect(Collectors.toList()),
                    bonus
            );
            MINES.put(mine.getLevel(), mine);

        });
        log.info("Loaded {} mines!", MINES.size());

        PointEvent pointEvent = eventManager.getPointEvent(REGIONS.values());
        Schedules.runAsync(() -> {
            AsyncCatcher.enabled = false;
            MINES.values().forEach(Mine::reset);
            AsyncCatcher.enabled = true;

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Moscow")));

            if (calendar.get(Calendar.HOUR_OF_DAY) == 19) {
                if (eventManager.getOtherEvents().containsKey("point"))
                    return;

                pointEvent.start();
            }
        }, 20, 20);

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
                "blocks",
                "players"
        );

        LeaderBoard prestige = new LeaderBoard(
                "&bТоп по престижам",
                ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("tops-prestige")),
                "prestige",
                "players"
        );

        LeaderBoard gang = new LeaderBoard(
                "&bТоп по бандам",
                ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("tops-gang")),
                "score",
                "gangs"
        );

        SimpleHolographic info = new SimpleHolographic(ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("info")));

        info.addTextLine("§fДобро пожаловать на §bOpPrison§f!");
        info.addEmptyLine();
        info.addTextLine("§fВаша первая шахта §7> §f/warp §7> §fШахты для групп §7> §7MANTLE §fшахта");
        info.addTextLine("§fНа данном режиме цель является прокачать свою кирку и престиж §8§o(/prestige|pr max)");
        info.addTextLine("§fА остальную информацию Вы можете узнать через §b/opprison");
        info.addEmptyLine();
        info.addTextLine("§fЖелаем удачи Вам в ваших начинаниях!");
        LeaderBoard.holograms.add(info);

        SimpleHolographic caseHere = new SimpleHolographic(ConfigUtils.loadLocationFromConfigurationSection(miscConfig.getConfigurationSection("caseHere")));

        caseHere.addTextLine("   §b^^^   ");
        caseHere.addTextLine("§fКейсы");

        LeaderBoard.holograms.add(caseHere);

        Schedules.runAsync(() -> {
            if (Bukkit.getOnlinePlayers().size() == 0)
                return;

            Bukkit.getOnlinePlayers().forEach(player -> {
              playerDataManager.updateTop(player);
            });

            blocks.update();
            prestige.update();

            if (getGangDataManager().getGangDataMap().isEmpty()) return;

            getGangDataManager().getGangDataMap().forEach((s, gangData) ->
                    GangDataSQL.save(
                            s,
                            Base64.getEncoder().encodeToString(getGangDataManager().getGangPlayers(gangData.getGangPlayerMap()).getBytes()),
                            gangData.getScore(),
                            gangData.saveVault()
                    )
            );

            gang.update();
        }, 20 * 300, 20 * 300);
    }

    private void loadEffects() {
        Bukkit.getScheduler().runTaskTimer( this, () ->
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getWorld().getName().contains("gangs"))
                        return;

                    String item = Items.getItemName(player.getInventory().getItemInMainHand());

                    if (item.length() < 1 || !item.equals("pickaxe"))
                        return;

                    Pickaxe pickaxe = PickaxeManager.pickaxes.get(player.getName());
                    val upgrades = pickaxe.getUpgrades();

                    int hasteLevel = (int) upgrades.get(Upgrade.HASTE).getCount() - 1;
                    int speedLevel = (int) upgrades.get(Upgrade.SPEED).getCount() - 1;
                    int jump_boostLevel = (int) upgrades.get(Upgrade.JUMP_BOOST).getCount() - 1;
                    int night_visionLevel = (int) upgrades.get(Upgrade.NIGHT_VISION).getCount() - 1;

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
        ThreadLocalRandom random0 = ThreadLocalRandom.current();

        Schedules.runAsync(
                () -> {
                    Map<String, BlockEvent> blockEMap = eventManager.getBlockEvents();
                    if (!blockEMap.isEmpty())
                        return;

                    switch (random0.nextInt(0, 1)) {
                        case 0: {
                            Predicate<Player> predicate = player -> getPlayerDataManager().getPlayerDataMap().get(player.getName()).getPrestige() >= 0;

                            if (Bukkit.getOnlinePlayers().stream().filter(predicate).count() < 1) {
                                ChatUtil.broadcast("");
                                ChatUtil.broadcast("   &fСобытие &aСостязание &fне началось из-за недостатка игроков.");
                                ChatUtil.broadcast("   &fУсловие: 4 игрока с 75M престижей.");
                                ChatUtil.broadcast("");
                                return;
                            }

                            Map<String, Double> blocks = eventManager.getBlocks();

                            scoreboardManager
                                    .line(
                                            1,
                                            ""
                                    )
                                    .reloadScoreboard();

                            BukkitTask task = Schedules.runAsync(() -> {
                                if (Bukkit.getOnlinePlayers().isEmpty())
                                    return;

                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    String playerName = player.getName();

                                    if (!blocks.containsKey(playerName))
                                        blocks.put(playerName, 0.0);

                                    scoreboardManager
                                            .getBaseScoreboard()
                                            .updateScoreboardLine(
                                                    2,
                                                    player,
                                                    "§a> §aБлоков: §f" + (blocks.containsKey(playerName) ? StringUtils.replaceComma(blocks.get(playerName)) : "0")
                                            );
                                });
                            }, 5, 5);

                            BlockEvent.get()
                                    .id("contest")
                                    .name("&aСостязание")
                                    .description(
                                            "   Суть том, чтобы телепортироваться на шахту &a75M &fпрестижей",
                                            "   и накопать больше всех блоков за 20 минут"
                                    )
                                    .consumer(event -> {
                                        Player player = event.getPlayer();
                                        String playerName = player.getName();

                                        if (OpPrison.MINES.get(-1).getBlocks().stream().noneMatch(resourceBlock -> resourceBlock.getType() == event.getBlock().getType()))
                                            return;

                                        if (blocks.isEmpty() || !blocks.containsKey(playerName))
                                            blocks.put(playerName, 0.0);

                                        blocks.replace(playerName, blocks.get(playerName) + 1);
                                    })
                                    .runnable((blockEvent) -> () -> {
                                        int[] i = {1};

                                        ChatUtil.broadcast("");

                                        val blockz = blocks.entrySet()
                                                .stream()
                                                .sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))
                                                .limit(3);

                                        if (!blockz.allMatch(stringDoubleEntry -> stringDoubleEntry.getValue() == 0)) {
                                            blockz.forEachOrdered(x -> {
                                                ChatUtil.broadcast("   &7%s. &b%s &f- &b%s &8&o(+%s%)", i[0], x.getKey(), StringUtils.replaceComma(x.getValue()), 15 / i[0]);
                                                i[0]++;

                                                if (Bukkit.getPlayer(x.getKey()) == null || !Bukkit.getPlayer(x.getKey()).isOnline())
                                                    return;

                                                PlayerData playerData = getPlayerDataManager().getPlayerDataMap().get(x.getKey());

                                                if (playerData == null)
                                                    return;

                                                double added = playerData.getToken() * 0.15 / i[0];

                                                playerData.addToken(added);
                                                RPlayer.checkAndGet(x.getKey()).getAchievements().addAchievement(Achievement.OP_CONTEST_WINNER);

                                                Question question = playerData.getQuestions().get("SOFOS");

                                                if (question == null)
                                                    return;

                                                Question.QuestionStep step = question.getStep();

                                                if (step == null)
                                                    return;

                                                if (step == Question.QuestionStep.COMPLETING) {
                                                    question.setStep(Question.QuestionStep.ALR_COMPLETED);
                                                    PickaxeManager.getPickaxes().get(x.getKey()).getUpgrades().get(Upgrade.JACK_HAMMER).setCompleteQ(true);
                                                }
                                            });
                                        } else
                                            ChatUtil.broadcast("   &fВ событии &aСостязание &fникто не принял участие");

                                        ChatUtil.broadcast("");

                                        blocks.clear();
                                        task.cancel();

                                        scoreboardManager
                                                .removeLine(2)
                                                .removeLine(1)
                                                .reloadScoreboard();

                                        blockEvent.stop();
                                    })
                                    .start(20 * 60 * 1);
                            break;
                        }

                        case 1: {
                            Map<String, List<Location>> treasureMap = eventManager.getTreasureMap();

                            BlockEvent.get()
                                    .id("treasure")
                                    .name("Искатель Сокровищ")
                                    .description("   Суть события в том, что нужно копать блоки и находить сокровища")
                                    .consumer(event -> {
                                        Player player = event.getPlayer();
                                        String playerName = player.getName();
                                        if (!treasureMap.containsKey(playerName))
                                            treasureMap.put(playerName, new ArrayList<>());

                                        float random = random0.nextFloat();

                                        if (random <= 0.01) {
                                            event.setCancelled(true);

                                            Block block = event.getBlock();

                                            block.setType(Material.CHEST);
                                            event.getPlayer().sendTitle("", "§aВы нашли сокровище", 5, 20, 5);
                                            treasureMap.get(playerName).add(block.getLocation());
                                        }
                                    })
                                    .runnable(blockEvent -> () -> {
                                        treasureMap.forEach((s, locations) -> {
                                            locations.forEach(location -> {
                                                location.getBlock().setType(Material.AIR);
                                            });
                                        });

                                        treasureMap.clear();
                                        blockEvent.stop();
                                    })
                                    .start(20 * 60 * 1);
                            break;
                        }

                        case 2: {
                            BoosterManager.addBooster(10);

                            BlockEvent.get()
                                    .id("booster")
                                    .name("Увеличенный Бустер")
                                    .description("   Суть события в том, что к бустеру сервера прибавляется 10%")
                                    .consumer(null)
                                    .runnable(blockEvent -> () -> {
                                        BoosterManager.delBooster(10);
                                        blockEvent.stop();
                                    })
                                    .start(20 * 60 * 1);
                            break;
                        }
                    }
                },
                20 * 60,
                20 * 60 * 5
        );
    }
}
