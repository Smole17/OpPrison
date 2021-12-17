package ru.smole.utils.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import ru.smole.utils.StringUtils;

public class ConfigUtils {
    public static Location loadLocationFromConfigurationSection(ConfigurationSection section) {
        if (section == null)
            return null;
        if (Bukkit.getWorld(section.getString("world")) == null)
            return null;
        World world = Bukkit.getWorld(section.getString("world"));
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        return new Location(world, x, y, z);
    }

    public static Location loadLocationFromConfigurationSectionSplit(ConfigurationSection section) {
        String[] split = section.getString("location").split(" ");
        World world = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat(split[4]);
        float pitch = Float.parseFloat(split[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
