package ru.smole.data.booster;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.util.ChatUtil;

import static ru.smole.OpPrison.*;

public class BoosterManager {

    private Player player;

    public BoosterManager(Player player) {
        this.player = player;
    }

    public void load() {
        int id = RPlayer.checkAndGet(player.getName()).getMainPermissionGroup().getId();

        switch (id) {
            case 10:
                addBooster(0.05);
                break;
            case 20:
                addBooster(0.1);
                break;
            case 30:
                addBooster(0.15);
                break;
            case 40:
                addBooster(0.2);
                break;
            case 50:
                addBooster(0.3);
                break;
            case 60:
                addBooster(0.4);
                break;
            case 70:
                addBooster(0.6);
                break;
            case 80:
                addBooster(0.7);
                break;
            case 90:
                addBooster(1.0);
                break;
            case 100:
            case 901:
                addBooster(2.0);
                break;  
        }

        BAR.setTitle(String.format("§fБустер сервера: §b+%s §8§o(/help booster)",
                StringUtils._fixDouble(1, BOOSTER) + "%"));

        Player neSmole = Bukkit.getPlayer("NeSmole");
        if (neSmole != null)
            ChatUtil.sendMessage(neSmole, PREFIX + "&bBAR UPDATE: " + BOOSTER);
    }

    public void unload() {
        int id = RPlayer.checkAndGet(player.getName()).getMainPermissionGroup().getId();

        switch (id) {
            case 40:
                delBooster(0.2);
                break;
            case 50:
                delBooster(0.3);
                break;
            case 60:
                delBooster(0.4);
                break;
            case 70:
                delBooster(0.6);
                break;
            case 80:
                delBooster(0.7);
                break;
            case 90:
                delBooster(1.0);
                break;
            case 901:
                delBooster(2.0);
                break;
        }

        BAR.setTitle(String.format("§fБустер сервера: §b+%s §8§o(/help booster)",
                StringUtils._fixDouble(1, BOOSTER) + "%"));
    }

    protected void addBooster(double count) {
        if (BOOSTER < 0) {
            BOOSTER = 0.0;
            return;
        }

        BOOSTER = BOOSTER + count;

        if (BOOSTER < 0) {
            BOOSTER = 0.0;
        }
    }

    protected void delBooster(double count) {
        if (BOOSTER < 0) {
            BOOSTER = 0.0;
            return;
        }

        BOOSTER = BOOSTER - count;

        if (BOOSTER < 0) {
            BOOSTER = 0.0;
        }
    }
}
