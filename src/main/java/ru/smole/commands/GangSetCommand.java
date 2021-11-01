package ru.smole.commands;

import lombok.val;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.gang.GangData;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

@CommandPermission(permission = "opprison.admin")
public class GangSetCommand extends BukkitCommand<Player> {
    public GangSetCommand() {
        super("gset", "gangset");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        switch (args.length) {
            case 2:
                String gName = args[0];

                val gangDataMap = OpPrison.getInstance().getGangDataManager().getGangDataMap();

                if (!gangDataMap.containsKey(gName)) {
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Unknown gang");
                    break;
                }

                GangData gangData = gangDataMap.get(gName);

                double score;
                try {
                    score = Double.parseDouble(args[1]);
                } catch (Exception exception) {
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Try to use double format");
                    break;
                }

                gangData.setScore(score);

                break;

            default: break;
        }
    }
}
