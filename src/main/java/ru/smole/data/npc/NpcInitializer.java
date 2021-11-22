package ru.smole.data.npc;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.xfenilafs.core.protocollib.entity.impl.FakePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class NpcInitializer {

    public static List<FakePlayer> npcList = new ArrayList<>();

    private final FakePlayer fakePlayer;

    public static void init() {
    }

    public static NpcInitializer createNpc(String skinName, Location location, String topName) {
        FakePlayer fakePlayer = new FakePlayer(location, topName);

        fakePlayer.setSkin(skinName);
        fakePlayer.setCustomName(topName);

        fakePlayer.spawn();

        npcList.add(fakePlayer);
        return new NpcInitializer(fakePlayer);
    }

    public void handleClick(Consumer<Player> handleClick) {
        if (handleClick != null) {
            fakePlayer.setClickAction(handleClick);
        }
    }
}
