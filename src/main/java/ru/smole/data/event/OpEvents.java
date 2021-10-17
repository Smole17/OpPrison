package ru.smole.data.event;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.smole.OpPrison;
import ru.smole.data.booster.BoosterManager;
import ru.smole.data.npc.question.Question;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

@UtilityClass
public class OpEvents {

    private final @Getter List<String> activeEvents = new ArrayList<>();
    private final @Getter Map<String, Consumer<AsyncPlayerChatEvent>> chatEvents = new HashMap<>();
    private final @Getter Map<String, Consumer<BlockBreakEvent>> breakEvents = new HashMap<>();
    private final @Getter Map<String, List<Location>> treasureMap = new HashMap<>();

    public void start(String name) {
        activeEvents.add(name);
    }

    public void stop(String name) {
        activeEvents.remove(name);
    }

    public void asyncChat(AsyncPlayerChatEvent event) {
        if (!chatEvents.isEmpty()) {
            chatEvents.forEach((s, asyncPlayerChatEventConsumer) -> {
                if (activeEvents.contains(s)) {
                    asyncPlayerChatEventConsumer.accept(event);
                }
            });
        }
    }

    public void blockBreak(BlockBreakEvent event) {
        if (!breakEvents.isEmpty()) {
            breakEvents.forEach((s, blockBreakEventConsumer) -> {
                if (breakEvents.containsKey(s)) {
                    if (blockBreakEventConsumer != null) blockBreakEventConsumer.accept(event);
                }
            });
        }
    }

    public void applyBlockContest(Map<String, Double> blocks) {
        String name = "Состязание";

        if (breakEvents.containsKey(name))
            return;

        OpPrison main = OpPrison.getInstance();
        Predicate<Player> predicate = player -> main.getPlayerDataManager().getPlayerDataMap().get(player.getName()).getPrestige() >= 75000000;

        if (Bukkit.getOnlinePlayers().stream().filter(predicate).count() < 4) {
            ChatUtil.broadcast("");
            ChatUtil.broadcast("   Событие &b" + name + " &fне началось из-за недостатка игроков");
            ChatUtil.broadcast("   Условие: 4 игрока с 75M престижей");
            ChatUtil.broadcast("");
            return;
        }

        breakEvents.put(name, event -> {
            Player player = event.getPlayer();
            String playerName = player.getName();

            if (OpPrison.MINES.get(-1).getBlocks().stream().noneMatch(resourceBlock -> resourceBlock.getType() == event.getBlock().getType()))
                return;

            if (blocks.isEmpty() || !blocks.containsKey(playerName))
                return;

            blocks.replace(playerName, blocks.get(playerName) + 1);
        });

        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Событие &b" + name + " &fначалось");
        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Суть события в том, чтобы телепортироваться на шахту &a75M &fпрестижей");
        ChatUtil.broadcast("   и накопать больше всех блоков за 20 минут");
        ChatUtil.broadcast("");

        Bukkit.getOnlinePlayers().stream().filter(predicate).forEach(player -> blocks.put(player.getName(), 0.0));

        Bukkit.getScheduler().runTaskLater(
                OpPrison.getInstance(),
                () -> {
                    ChatUtil.broadcast("");
                    ChatUtil.broadcast("   Событие &b" + name + " &fзавершилось");
                    ChatUtil.broadcast("");

                    int[] i = {1};

                    blocks.entrySet()
                            .stream()
                            .sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))
                            .limit(3)
                            .forEachOrdered(x -> {
                                ChatUtil.broadcast("   &7%s. &b%s &f- &b%s", i[0], x.getKey(), StringUtils.replaceComma(x.getValue()));
                                i[0]++;

                                if (Bukkit.getPlayer(x.getKey()) == null|| !Bukkit.getPlayer(x.getKey()).isOnline())
                                    return;

                                PlayerData playerData = main.getPlayerDataManager().getPlayerDataMap().get(x.getKey());

                                if (playerData == null)
                                    return;

                                double added = playerData.getToken() * 0.15 / i[0];

                                playerData.addToken(added);

                                Question question = playerData.getQuestions().get("SOFOS");
                                Question.QuestionStep step = question.getStep();

                                if (step == null)
                                    return;

                                if (step == Question.QuestionStep.COMPLETING) {
                                    question.setStep(Question.QuestionStep.ALR_COMPLETED);
                                }
                            });

                    ChatUtil.broadcast("");

                    breakEvents.remove(name);
                    blocks.clear();
                    BoosterManager.updateBar();
                },
                20 * 60 * 20
        );

        BoosterManager.updateBar();
    }
    
    public void applyBoosterEvent() {
        String name = "Увеличенный бустер";

        if (breakEvents.containsKey(name))
            return;

        breakEvents.put(name, null);

        BoosterManager.addBooster(10);

        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Событие &b" +  name + " &fначалось");
        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Суть события в том, что к бустеру сервера прибавляется 10%");
        ChatUtil.broadcast("");

        Bukkit.getScheduler().runTaskLater(
                OpPrison.getInstance(),
                () -> {
                    ChatUtil.broadcast("");
                    ChatUtil.broadcast("   Событие &b" + name + " &fзавершилось");
                    ChatUtil.broadcast("");

                    BoosterManager.delBooster(10);
                    breakEvents.remove(name);
                    BoosterManager.updateBar();
                },
                20 * 60 * 20
        );

        BoosterManager.updateBar();
    }

    public void applyTreasureHunter() {
        String name = "Искатель сокровищ";

        if (breakEvents.containsKey(name))
            return;

        ThreadLocalRandom randomO = ThreadLocalRandom.current();
        breakEvents.put(name, event -> {
            Player player = event.getPlayer();
            String playerName = player.getName();
            if (!treasureMap.containsKey(playerName))
                treasureMap.put(playerName, new ArrayList<>());

            float random = randomO.nextFloat();

            if (random <= 0.01) {
                event.setCancelled(true);

                Block block = event.getBlock();

                block.setType(Material.CHEST);
                event.getPlayer().sendTitle("", "§aВы нашли сокровище", 5, 20, 5);
                treasureMap.get(playerName).add(block.getLocation());
            }
        });

        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Событие &b" +  name + " &fначалось");
        ChatUtil.broadcast("");
        ChatUtil.broadcast("   Суть события в том, что нужно копать блоки и находить сокровища");
        ChatUtil.broadcast("");

        Bukkit.getScheduler().runTaskLater(
                OpPrison.getInstance(),
                () -> {
                    ChatUtil.broadcast("");
                    ChatUtil.broadcast("   Событие &b" + name + " &fзавершилось");
                    ChatUtil.broadcast("");

                    breakEvents.remove(name);
                    treasureMap.clear();
                    BoosterManager.updateBar();
                },
                20 * 60 * 20
        );

        BoosterManager.updateBar();
    }
}
