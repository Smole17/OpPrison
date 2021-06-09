package ru.smole.commands;

import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.PlayerDataManager;
import ru.smole.items.Items;
import ru.smole.player.OpPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.command.BukkitMegaCommand;
import ru.xfenilafs.core.util.ChatUtil;

public class TokenCommand extends BukkitMegaCommand<Player> {
    public TokenCommand() {
        super("token");
    }

    private final PlayerDataManager dataManager = OpPrison.getInstance().getPlayerDataManager();

    @Override
    protected void onUsage(Player player) {
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());

        ChatUtil.sendMessage(player, "&bOpPrison > ⛃%s", StringUtils._formatDouble(playerData.getToken()));
    }

    @CommandArgument(aliases = {"сконвертировать"})
    public void withdraw(Player player, String... args) {
        int token = 0;
        try {
            token = Integer.parseInt(args[1]);
        } catch (Exception e) {
            ChatUtil.sendMessage(player, "&bOpPrison > &cВведите число!");
        }

        withdrawPlayer(player, token);
    }

    public void withdrawPlayer(Player player, int count) {
        OpPlayer opPlayer = new OpPlayer(player);
        PlayerData playerData = dataManager.getPlayerDataMap().get(player.getName());
        double token = playerData.getToken();

        if (count <= 0) {
            opPlayer.sendMessage("Введите корректное число");
            return;
        }

        if (count < token) {
            opPlayer.sendMessage("Недостаточно токенов");
            return;
        }

        opPlayer.add(Items.getToken(count));
        opPlayer.sendMessage("Вы успешно конвертировали в предмет");
    }
}
