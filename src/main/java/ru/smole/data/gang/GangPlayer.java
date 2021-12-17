package ru.smole.data.gang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
public class GangPlayer {
    private String playerName;
    private GangPlayerType type;
    private double score;

    @AllArgsConstructor
    @Getter
    public enum GangPlayerType {

        DEFAULT("§8Участник"),
        OLDEST("§7Старейшина"),
        MANAGER("§fСоруководитель"),
        LEADER("§f§lГлава");

        private final String name;

        public static GangPlayerType getTypeFromOrdinal(int ordinal) {
            for (GangPlayerType type : GangPlayerType.values()) {
                if (type.ordinal() == ordinal)
                    return type;
            }

            return null;
        }
    }

    public boolean upType() {
        int ordinal = type.ordinal();
        GangPlayerType upped = GangPlayerType.getTypeFromOrdinal(ordinal + 1);

        if (upped == null)
            return false;

        setType(upped);
        return true;
    }
}
