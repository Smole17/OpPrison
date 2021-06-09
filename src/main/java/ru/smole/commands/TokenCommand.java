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

public class TokenCommand extends BukkitCommand<Player> {
    public TokenCommand() {
        super("token");
    }

    private PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @Override
    protected void onExecute(Player player, String[] args) {
        OpPlayer opPlayer = new OpPlayer(player);
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                opPlayer.sendMessage("Игрок не найден");
                return;
            }
            String targetName = target.getName();
            PlayerData targetData = dataManager.getPlayerDataMap().get(targetName);

            opPlayer.sendMessage(targetName + ": ⛃" + StringUtils._formatDouble(targetData.getToken()));
            return;
        }

        if (args.length == 2) {
            if (args[0].equals("withdraw")) {
                int token = 0;
                try {
                    token = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    opPlayer.sendMessage("Введите число");
                }

                withdraw(player, token);
                return;
            }
        }

        opPlayer.sendMessage("⛃" + StringUtils._formatDouble(playerData.getToken()));
    }

    public void withdraw(Player player, int count) {
        OpPlayer opPlayer = new OpPlayer(player);
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        double token = playerData.getToken();

        if (count <= 0) {
            opPlayer.sendMessage("Введите корректное число");
            return;
        }

        if (token < count) {
            opPlayer.sendMessage("Недостаточно токенов");
            return;
        }

        opPlayer.add(Items.getToken(count));
        opPlayer.sendMessage("Вы успешно конвертировали в предмет");
    }
}
