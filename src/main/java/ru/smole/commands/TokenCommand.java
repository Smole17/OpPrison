package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.player.PlayerData;
import ru.smole.data.player.PlayerDataManager;
import ru.smole.data.player.OpPlayer;
import ru.smole.data.items.Items;
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
                ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Игрок не найден");
                return;
            }
            String targetName = target.getName();
            PlayerData targetData = dataManager.getPlayerDataMap().get(targetName);

            ChatUtil.sendMessage(player, OpPrison.PREFIX + targetName + ": ⛃" + StringUtils.replaceComma(targetData.getToken()));
            return;
        }

        if (args.length == 2) {
            if (args[0].equals("withdraw")) {
                double token;
                try {
                    if (args[1].contains(".")) {
                        ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Введите целое положительное число");
                        return;
                    }

                    token = Double.parseDouble(args[1]);
                } catch (Exception e) {
                    ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Введите целое положительное число");
                    return;
                }

                withdraw(player, token);
                return;
            }
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + "⛃" + StringUtils.replaceComma(playerData.getToken()));
    }

    public void withdraw(Player player, double count) {
        OpPlayer opPlayer = new OpPlayer(player);
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        double token = playerData.getToken();

        if (1D >= count) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Введите корректное число");
            return;
        }

        if (count > token) {
            ChatUtil.sendMessage(player, OpPrison.PREFIX_N + "Недостаточно токенов");
            return;
        }

        opPlayer.add(Items.getItem("token", count));
        playerData.setToken(token - count);
        ChatUtil.sendMessage(player, OpPrison.PREFIX + "Вы успешно конвертировали в предмет");
    }
}
