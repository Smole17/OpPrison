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

        description = "Руководство не найдено. Используйте данный список: " + Arrays.toString(Guide.values());

        if (args.length >= 1) {
            try {
                Arrays
                        .stream(Guide.values())
                        .filter(guide -> Guide.getGuideFromString(args[0]) == guide)
                        .forEach(guide ->
                                description = guide.getDesc()
                        );
            } catch (Exception e) {
                description = "Руководство не найдено. Используйте данный список: " + Arrays.toString(Guide.values());
            }
        }

        ChatUtil.sendMessage(player, OpPrison.PREFIX + String.format("Найденная информация: %s", description));
    }

    @AllArgsConstructor
    public enum Guide {

        BOOSTER(
                "\n   Бустеры в зависимости от процента, " +
                "\n   дают бонус к выпадаемым токенам и монетам."
        ),

        GROUP(
                "\n   Группы - это некие привилегии данного режима, " +
                "\n   которые Вы можете получить из ящиков." +
                "\n\n   §8(привилегии режима не связаны со всеми режимами сервера)"
        ),

        TOKEN(
                "\n   Токены - это одна из валют режима," +
                "\n   которая требуется на данный момент для прокачки кирки."
        ),

        MONEY(
                "\n   Деньги - это одна из валют режима," +
                "\n   которая требуется на данный момент для прокачки" +
                "\n    престижей. Добываемые деньги умножаются на кол-во" +
                        "\n   множителя, который указан в скорборде."
        ),

        KEY(
                "\n   Ключи - предметы, которые нужны для открытия кейсов." +
                        "\n   С кейсов можно деньги, токены, ящики и много другого."
        ),

        CRATES(
                "\n   Ящики - предметы, с помощью которых можно выбить" +
                        "\n   много разнообразных предметов, которые будут круче" +
                        "\n   чем в кейсах."
        ),

        PICKAXE(
                "\n   Кирка - Ваш основной инструмент. Вы можете на ней прокачивать" +
                        "\n   чары/прокачки, с помощью которых приумножать добываемые валюты" +
                        "\n   режима, а также добывать ключи"
        ),

        GAME(
                "\n   OpPrison - идея была взята с запада. " +
                        "\n   Тут основное внимание сконцентрированно на копание, прокачку престижей" +
                        "\n   и прокачку кирки в 100x100x100 шахтах." +
                        "\n   Локации доступны через /warp"
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
