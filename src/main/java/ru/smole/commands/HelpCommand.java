package ru.smole.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import ru.smole.OpPrison;
import ru.xfenilafs.core.command.BukkitCommand;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.Arrays;

public class HelpCommand extends BukkitCommand<Player> {

    public HelpCommand() {
        super("help");
    }

    public static String description;

    @Override
    protected void onExecute(Player player, String[] args) {

        description = "§fИспользуйте §a/help [Аргумент]. §fСписок аргументов: §a" + Arrays.toString(Guide.values());

        if (args.length >= 1) {
            try {
                Arrays
                        .stream(Guide.values())
                        .filter(guide -> Guide.getGuideFromString(args[0]) == guide)
                        .forEach(guide ->
                                description = guide.getDesc()
                        );
            } catch (Exception e) {
                description = "§fИспользуйте §a/help [Аргумент]. §fСписок аргументов: §a" + Arrays.toString(Guide.values());
            }
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + String.format("Найденная информация: %s", description));
    }

    @AllArgsConstructor public enum Guide {

        BOOSTER(
                "\n" +
                "\n   Бустеры в зависимости от процента, " +
                "\n   дают бонус к выпадению §eтокенов §fи §aмонет." +
                "\n" +
                "\n   Вечный множитель от привилегий StarFarm:" +
                "\n    §fVIP - §a5%" +
                "\n    §fVIP+ - §a10%" +
                "\n    §fPREMIUM - §a15%" +
                "\n    §fPREMIUM+ - §a20%" +
                "\n    §fELITE - §a30%" +
                "\n    §fELITE+ - §a40%" +
                "\n    §fSPONSOR - §a60%" +
                "\n    §fSPONSOR+ - §a80%" +
                "\n    §fPREMIUM+ - §a100%" +
                "\n"
        ),

        GROUP(
                "\n" +
                "\n   §cГруппы §f- это некие привилегии данного режима, " +
                "\n   которые Вы можете получить из ящиков." +
                "\n   Привилегии режима никак не связаны с общим сервером." +
                "\n" +
                "\n   §cГруппы §fдают вам доступ к новым шахтам, " +
                "\n   китам и специальным возможностям." +
                "\n"
        ),

        TOKEN(
                "\n" +
                "\n   Токены - это одна из валют режима," +
                "\n   которая требуется для прокачки кирки." +
                "\n"
        ),

        MONEY(
                "\n" +
                "\n   §aДеньги §f- это одна из валют режима," +
                "\n   которая требуется для прокачки престижей," +
                "\n   таким образов открывая новые локации." +
                "\n" +
                "\n   Добываемые §aденьги §fумножаются на кол-во" +
                "\n   множителя, который указан в скорборде." +
                "\n"
        ),

        KEY(
                "\n" +
                "\n   §aКлючи §f- предметы, которые нужны для открытия кейсов." +
                "\n   С §aкейсов §fможно выбить §aденьги§f, §eтокены§f, §5ящики §fи много другого." +
                "\n"
        ),

        CRATES(
                "\n" +
                "\n   §5Ящики §f- предметы, открыв которые можно выбить" +
                "\n   много разнообразных редких предметов." +
                "\n"
        ),

        PICKAXE(
                "\n" +
                "\n   §aКирка §f- Ваш основной инструмент. Прокачивайте на вашей кирке" +
                "\n   различные чары/улучшения за §eтокены§f, с помощью которых можно" +
                "\n   приумножать добываемые валюты режима и ключи." +
                "\n"
        ),

        GAME(
                "\n" +
                "\n   §bPrison OP §f- классический кликер в тюремном стиле с шахтами, " +
                "\n   который был создан, вдохновившись западными режимами этой тематики." +
                "\n   Тут основное внимание сконцентрировано на плавное и приятное копание," +
                "\n   прокачку престижей, улучшение кирок, а так же соревнование с гильдиями других игроков. " +
                "\n"
        );

        private @Getter String desc;

        public static Guide getGuideFromString(String guide) {
            for (Guide type : Guide.values()) {
                if (type == Guide.valueOf(guide.toUpperCase())) {
                    return type;
                }
            }

            return null;
        }
    }
}
