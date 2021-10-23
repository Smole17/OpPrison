package ru.smole.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import ru.luvas.rmcs.MainClass;
import ru.smole.OpPrison;
import ru.smole.mines.Mine;
import ru.xfenilafs.core.command.sexy.Parameter;
import ru.xfenilafs.core.command.sexy.SexyCommand;
import ru.xfenilafs.core.command.sexy.context.CommandContext;
import ru.xfenilafs.core.command.sexy.require.Require;
import ru.xfenilafs.core.command.sexy.type.TypeString;
import sexy.kostya.mineos.network.client.SexyClient;
import sexy.kostya.mineos.network.packets.Packet36RankChange;
import sexy.kostya.mineos.perms.PermissionGroup;

import java.util.stream.Collectors;

public class MineCommand extends SexyCommand<CommandSender> {

    public MineCommand() {
        super("mines");
        addRequire(new Require() {
            public boolean can(CommandSender commandSender) {
                return commandSender.hasPermission("prison.mines");
            }

            public String message(CommandSender commandSender) {
                return "§cНедостатачно прав";
            }
        });
        setPrefix("");
        addParameter(new Parameter<>("Шахта", new TypeString()));
    }

    protected void process(CommandContext<CommandSender> ctx) {
        String mine = ctx.getArg(0);
        if (mine.equalsIgnoreCase("list"))
            ctx.sendMessage(
                    "Список шахт §7- §a%s",
                    OpPrison.MINES.keySet().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("§7, §a"))
            );
        else {
            Mine m = OpPrison.MINES.get(Integer.parseInt(mine));
            if (m == null)
                ctx.sendMessage("Шахта §c%s§f не найдена", mine);
            else {
                m.fill();
                ctx.sendMessage("Заполняю шахту §a%s §7- §a%s", mine, ((CraftWorld) m.getZone().getWorld()).getHandle().captureBlockStates);
            }
        }
    }

}
