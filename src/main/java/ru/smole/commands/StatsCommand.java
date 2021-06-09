package ru.smole.commands;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.player.OpPlayer;
import ru.smole.rank.Rank;
import ru.smole.rank.RankManager;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

@CommandPermission(permission = "opprison.admin")
public class StatsCommand extends BukkitCommand<Player> {
    public StatsCommand() {
        super("stats", "st", "statistic");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        String msg = OpPrison.PREFIX + "/stats name BLOCKS/MONEY/TOKEN/MULTIPLIER/PRESTIGE/RANK value";

        if (args.length == 3) {
            Player target = Bukkit.getPlayer(args[0]);
            String targetName = target.getName();

            RankManager rankManager = new OpPlayer(target).getRankManager();
            PlayerData targetData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(targetName);
            Stat type = Stat.getTypeFromString(args[1]);

            if (type == null) {
                ChatUtil.sendMessage(player, msg);
                return;
            }

            if (type == Stat.RANK) {
                Rank rank = rankManager.getRankFromString(args[2].toUpperCase());

                if (rank == null) {
                    ChatUtil.sendMessage(player, msg);
                    return;
                }

                targetData.setRank(rank);
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "������ %s ��� ���������� ���� %s", targetName, rank.getName());
                return;
            }

            int value;
            try {
                value = Integer.parseInt(args[2]);
            } catch (Exception e) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "������� ����� ������������� �����");
                return;
            }

            String piece = "null";

            if (type == Stat.BLOCKS) {
                targetData.setBlocks(value);
                piece = "���� ����������� ������";
            }

            if (type == Stat.MONEY) {
                targetData.setMoney(value);
                piece = "���� ����������� �����";
            }

            if (type == Stat.TOKEN) {
                targetData.setToken(value);
                piece = "���� ����������� �������";
            }

            if (type == Stat.MULTIPLIER) {
                targetData.setMultiplier(value);
                piece = "��� ���������� ���������";
            }

            if (type == Stat.PRESTIGE) {
                targetData.setPrestige(value);
                piece = "���� ����������� ���������";
            }

            ChatUtil.sendMessage(player, OpPrison.PREFIX + "������ %s %s: %s", targetName, piece, value);
        }

        ChatUtil.sendMessage(player, msg);
    }

    @AllArgsConstructor public enum Stat {

        BLOCKS(),
        MONEY(),
        TOKEN(),
        MULTIPLIER(),
        PRESTIGE(),
        RANK();

        public static Stat getTypeFromString(String stat) {
            for (Stat type : Stat.values())
                if (type == Stat.valueOf(stat))
                    return type;

            return null;
        }
    }
}
