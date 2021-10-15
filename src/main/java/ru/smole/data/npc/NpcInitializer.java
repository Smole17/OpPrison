package ru.smole.data.npc;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.items.pickaxe.PickaxeManager;
import ru.smole.data.items.pickaxe.Upgrade;
import ru.smole.data.npc.question.Question;
import ru.smole.data.player.PlayerData;
import ru.smole.utils.ChatUtil;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.protocollib.entity.FakeBaseEntity;
import ru.xfenilafs.core.protocollib.entity.impl.FakePlayer;
import ru.xfenilafs.core.util.mojang.MojangUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@AllArgsConstructor
public class NpcInitializer {

    public static List<FakePlayer> npcList = new ArrayList<>();

    private final FakePlayer fakePlayer;

    public static void init() {
        createNpc(
                "Telpochtli",
                    new Location(Bukkit.getWorld("mine_10"), 84.5, 182, 7.5, 90, 0),
                "§eСофос"
        ).handleClick(player -> {
            if (Bukkit.getScheduler().getPendingTasks().contains(ChatUtil.task))
                return;

            String name = player.getName();
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(name);

            if (playerData.getPrestige() < 75000000)
                return;

            Map<String, Question> questions = playerData.getQuestions();

            if (!questions.containsKey("SOFOS") || PickaxeManager.getPickaxes().get(player.getName()).getUpgrades().get(Upgrade.JACK_HAMMER).isCompleteQ()) {
                ChatUtil.sendTaskedMessage(
                        player,
                        4,
                        String.format("§8[&eСофос§8] §fЗдравствуй %s! Меня зовут Софос.", name),
                        "§8[&eСофос§8] §fЯ являюсь хранителем данного облака. Ты славно постарался, чтобы дойти сюда",
                        "§8[&eСофос§8] §fМоя семья испокон веков наделяла возможность получить уникальное зачарование всех пришедших гостей",
                        "§8[&eСофос§8] §fДля того чтобы получить чудесное знание, тебе предстоит выиграть состязание в копании",
                        "§8[&eСофос§8] §fДанное соревнование происходит каждые 40 минут и длительность составляет 20",
                        "§8[&eСофос§8] §fЕго суть заключается в том, чтобы накопать больше блоков в шахте на данном облаке",
                        "§8[&eСофос§8] §fТакже помимо получения навыка, ты можешь выигрывать токены в зависимости от занимаемого места",
                        "§8[&eСофос§8] §fЖелаю тебе удачи, мой юный друг!"
                );

                if (!questions.containsKey("SOFOS") || questions.get("SOFOS") != null)
                    questions.put("SOFOS", new Question(Question.QuestionStep.COMPLETING));
            }

            if (!Bukkit.getScheduler().getPendingTasks().contains(ChatUtil.task)) {
                ru.xfenilafs.core.util.ChatUtil.sendMessage(player, "§8[&eСофос§8] §fЗдраствуй %s! Как твои дела?", name);
            }
        });
    }

    public static NpcInitializer createNpc(String skinName, Location location, String topName) {
        FakePlayer fakePlayer = new FakePlayer(location, topName);

        fakePlayer.setSkin(skinName);
        fakePlayer.setCustomName(topName);

        fakePlayer.spawn();

        npcList.add(fakePlayer);
        return new NpcInitializer(fakePlayer);
    }

    public void handleClick(Consumer<Player> handleClick) {
        if (handleClick != null) {
            fakePlayer.setClickAction(handleClick);
        }
    }
}
