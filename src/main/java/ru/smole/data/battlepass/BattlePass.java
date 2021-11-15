package ru.smole.data.battlepass;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.smole.OpPrison;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.player.PlayerData;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.*;
import java.util.function.Consumer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class BattlePass {

    private Map<Integer, BattlePassPlayer.BattlePassLevel> levels;
    private Map<Integer, BattlePassTask> tasks;

    public BattlePassPlayer.BattlePassLevel getLevel(int i) {
        return levels.getOrDefault(i, null);
    }

    public BattlePassTask getTask(int i) {
        return tasks.getOrDefault(i, null);
    }

    @AllArgsConstructor
    @Data
    public static class BattlePassPlayer {

        private double exp;
        private BattlePassLevel battlePassLevel;
        private Player player;
        private boolean premium;
        private List<BattlePassTask> tasks;

        public void addExp(double added) {
            exp = exp + added;
            tryUp();
        }

        private void tryUp() {
            val levels = OpPrison.getInstance().getBattlePass().getLevels();
            int level = Objects.requireNonNull(
                    levels.entrySet()
                            .stream()
                            .parallel()
                            .filter(entry -> entry.getValue().equals(battlePassLevel))
                            .findFirst()
                            .orElse(null))
                    .getKey();

            BattlePassLevel battlePassLevel = levels.get(level + 1);

            if (exp < battlePassLevel.getExp())
                return;

            this.battlePassLevel = battlePassLevel;
            this.battlePassLevel.getRewards(player);
        }

        @AllArgsConstructor
        @Data
        public static class BattlePassLevel {

            private int level;
            private double exp;
            private Reward[] rewards;

            public void getRewards(Player player) {
                Arrays.stream(rewards).forEach(reward -> reward.getReward(player));
            }

            @AllArgsConstructor
            @Data
            public static class Reward {

                private boolean premium;
                private ItemStack[] itemStacks;
                private int slot;

                public void getReward(Player player) {
                    Arrays.stream(itemStacks).forEach(itemStack -> {
                        OpPlayer.add(player, itemStack.clone());
                        ChatUtil.sendMessage(player,
                                OpPrison.PREFIX + "Вы получили новый предмет %s &ax%s",
                                itemStack.getItemMeta().getDisplayName(), itemStack.getAmount()
                        );
                    });
                }
            }
        }
    }

    @AllArgsConstructor
    public static class BattlePassTask {

        private @Getter @Setter int i;
        private @Getter @Setter double exp;
        private @Getter @Setter boolean premium;
        private @Getter boolean complete;
        private @Getter @Setter int time;
        private final @Getter TaskType taskType;

        public void complete(Player player) {
            this.complete = true;
            val level = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName()).getBattlePass();
            level.addExp(exp);
        }

        public BattlePassTask setComplete(boolean set) {
            this.complete = set;
            return this;
        }

        @AllArgsConstructor
        @Data
        public static class TaskType {

            private final Type type;
            private final double value;

            @AllArgsConstructor
            public enum Type {

                BLOCKS(Material.GOLD_PICKAXE),
                PRESTIGE(Material.BEACON),
                KILL(Material.DIAMOND_SWORD),
                SCORE(Material.SPONGE),
                RUN_COMMAND(Material.COMMAND),
                TAKE_DAMAGE(Material.SKULL_ITEM),
                REGENERATE(Material.GOLDEN_APPLE),
                TAKE_EVENTS(Material.CHEST),
                REACH_MULTIPLIER(Material.NETHER_STAR);


                private final @Getter Material material;
            }
        }
    }
}
