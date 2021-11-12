package ru.smole.data.gang;

import gnu.trove.map.TIntObjectMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.player.RPlayer;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.inventory.BaseInventory;
import ru.xfenilafs.core.inventory.BaseInventoryItem;
import ru.xfenilafs.core.inventory.handler.impl.BaseInventoryNotClickable;
import ru.xfenilafs.core.inventory.impl.BaseSimpleInventory;
import ru.xfenilafs.core.util.Base64Util;
import ru.xfenilafs.core.util.ChatUtil;
import sexy.kostya.mineos.achievements.Achievement;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.smole.OpPrison.PREFIX_N;

@Data
public class GangData {

    private String name;
    private Map<String, GangPlayer> gangPlayerMap;
    private double score;
    private Inventory vault;

    public GangData(String name, Map<String, GangPlayer> gangPlayerMap, double score, String data) {
        this.name = name;
        this.gangPlayerMap = gangPlayerMap;
        this.score = score;
        this.vault = Bukkit.createInventory(null, 6 * 9, String.format("Хранилище %s", name));

        loadVault(data);
    }

    public void sendMessage(String message) {
        gangPlayerMap.values().forEach(gangPlayer ->
                ChatUtil.sendMessage(gangPlayer.getPlayerName(),"&8[%s&8] &f" + message, name)
        );
    }

    public void addScore(double count) {
        double added = score + count;
        setScore(added);
    }

    public void addGangPlayer(GangPlayer gangPlayer) {
        if (isFull())
            return;

        gangPlayerMap.put(gangPlayer.getName().toLowerCase(), gangPlayer);

        RPlayer.checkAndGet(gangPlayer.getPlayerName()).getAchievements().addAchievement(Achievement.OP_GANG_JOIN);
    }

    public void removeGangPlayer(String playerName) {
        if (!gangPlayerMap.containsKey(playerName.toLowerCase()))
            return;

        gangPlayerMap.remove(playerName.toLowerCase());
    }

    public boolean isFull() {
        return gangPlayerMap.size() >= 10;
    }


    public GangPlayer getGangPlayer(String playerName) {
        return gangPlayerMap.get(playerName.toLowerCase());
    }

    public List<GangPlayer> findGangPlayers(GangPlayer.GangPlayerType type) {
        return gangPlayerMap.values().stream().filter(gangPlayer -> gangPlayer.type == type).collect(Collectors.toList());
    }

    public List<GangPlayer> findGangPlayers(GangPlayer.GangPlayerType... type) {
        List<GangPlayer> gangPlayerList = new ArrayList<>();

        for (GangPlayer.GangPlayerType gangPlayerType : type)
            gangPlayerList.addAll(findGangPlayers(gangPlayerType));

        return gangPlayerList;
    }

    public String saveVault() {
        return Base64Util.encodeItems(vault.getContents());
    }

    public void loadVault(String data) {
        if (data == null || data.isEmpty())
            return;

        vault.setContents(Base64Util.decodeItems(data));
    }

    public void openVault(GangPlayer player) {
        if (player.getType().ordinal() < GangPlayer.GangPlayerType.OLDEST.ordinal()) {
            ChatUtil.sendMessage(player.getPlayer(), PREFIX_N + "Ваша роль в банде слишком мала для этого действия");
            return;
        }

        player.getPlayer().openInventory(this.getVault());
    }

    public void openVault(Player player) {
        String playerName = player.getName();

        if (this.getGangPlayerMap().containsKey(playerName.toLowerCase()))
            openVault(this.getGangPlayer(playerName));
    }

    @AllArgsConstructor
    @Data
    public static class GangPlayer {

        private String name;
        private GangPlayerType type;
        private double score;

        public String getPlayerName() {
            return Bukkit.getOfflinePlayer(name).getName();
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(getPlayerName());
        }

        public void addScore(double added) {
            setScore(score + added);
        }

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

            if (upped == null || upped == GangPlayerType.LEADER)
                return false;

            setType(upped);
            return true;
        }

        public boolean deUpType() {
            int ordinal = type.ordinal();
            GangPlayerType deUpped = GangPlayerType.getTypeFromOrdinal(ordinal - 1);

            if (deUpped == null || deUpped == GangPlayerType.LEADER)
                return false;

            setType(deUpped);
            return true;
        }
    }
}
