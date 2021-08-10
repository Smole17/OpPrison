package ru.smole.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class WorldBorderUtils {

    public static void spawn(World world, Location center, double size) {
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(center);
        worldBorder.setSize(size);
        worldBorder.setDamageAmount(0);
    }
}
