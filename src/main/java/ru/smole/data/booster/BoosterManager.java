package ru.smole.data.booster;

import org.bukkit.entity.Player;
import ru.luvas.rmcs.player.RPlayer;
import ru.smole.OpPrison;
import sexy.kostya.mineos.perms.PermissionGroup;

import static ru.smole.OpPrison.BOOSTER;

public class BoosterManager {

    private Player player;

    public BoosterManager(Player player) {
        this.player = player;
    }

    public void load() {
        int id = RPlayer.checkAndGet(player.getName()).getMainPermissionGroup().getId();

        switch (id) {
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
        }
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
        }
    }

    protected void addBooster(double count) {
        BOOSTER = BOOSTER + count;
    }

    protected void delBooster(double count) { BOOSTER = BOOSTER - count; }
}
