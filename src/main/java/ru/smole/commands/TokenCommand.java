package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.items.Items;
import ru.smole.player.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

public class TokenCommand extends BukkitCommand<Player> {
    public TokenCommand() {
        super("token");
    }

    private PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @Override
    protected void onExecute(Player player, String[] args) {
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                ChatUtil.sendMessage(player, OpPrison.PREFIX + "Игрок не найден");
                return;
            }
            String targetName = target.getName();
            PlayerData targetData = dataManager.getPlayerDataMap().get(targetName);

            ChatUtil.sendMessage(player, OpPrison.PREFIX + targetName + ": ⛃" + StringUtils._formatDouble(targetData.getToken()));
            return;
        }

        if (args.length == 2) {
            if (args[0].equals("withdraw")) {
                int token = 0;
                try {
                    token = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    ChatUtil.sendMessage(player, OpPrison.PREFIX + "Введите число");
                }

                withdraw(player, token);
                return;
            }
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + "⛃" + StringUtils._formatDouble(playerData.getToken()));
    }

    public void withdraw(Player player, int count) {
        OpPlayer opPlayer = new OpPlayer(player);
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        double token = playerData.getToken();

        if (count <= 0) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Введите корректное число");
            return;
        }

        if (token < count) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX + "Недостаточно токенов");
            return;
        }

        opPlayer.add(Items.getToken(count));
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы успешно конвертировали в предмет");
    }
}
