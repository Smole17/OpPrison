package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.data.trade.Trade;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.command.annotation.CommandPermission;
import ru.xfenilafs.core.util.ChatUtil;

@CommandPermission(permission = "opprison.admin")
public class TradeCommand extends BukkitCommand<Player> {
    public TradeCommand() {
        super("trade");
    }

    @Override
    protected void onExecute(Player player, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName());
            Trade.toggleTradeEnabled(playerData);
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (!target.isOnline()) {
                ChatUtil.sendMessage(player, "&8[&2Торговля&8] &cУказанный игрок не в сети!");
                return;
            }
            PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(args[0]);
            if (Trade.isTrading(playerData)) {
                ChatUtil.sendMessage(player, "&8[&2Торговля&8] &cЭтот игрока уже с кем-то торгует!");
                return;
            }
            if (playerData.getName().equals(player.getName())) {
                ChatUtil.sendMessage(player, "&8[&2Торговля&8] &cВы не можете начать торг с самим собой!");
                return;
            }
            if (!Trade.isTradeEnabled(playerData)) {
                ChatUtil.sendMessage(player, "&8[&2Торговля&8] &cУказанному игроку нельзя предлагать торговлю. Чтобы это было возможно, он должен ввести &b/trade toggle!");
                return;
            }
            new Trade(OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(player.getName()), playerData);
        } else {
            ChatUtil.sendMessage(player, "&8[&2Торговля&8] &cИспользование: &b/trade [Ник игрока] / /trade toggle");
        }
    }
}