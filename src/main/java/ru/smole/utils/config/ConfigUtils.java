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
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void saveLocationToConfigurationSection(Location location, Config config, String sectionName) {
        ConfigurationSection section;
        if (config.getConfiguration().getConfigurationSection(sectionName) != null)
            section = config.getConfiguration().getConfigurationSection(sectionName);
        else
            section = config.getConfiguration().createSection(sectionName);
        section.set("world", location.getWorld().getName());
        section.set("x", StringUtils.fixDouble(1, location.getX()));
        section.set("y", StringUtils.fixDouble(1, location.getY()));
        section.set("z", StringUtils.fixDouble(1, location.getZ()));
        section.set("yaw", StringUtils.fixDouble(1, location.getYaw()));
        section.set("pitch", StringUtils.fixDouble(1, location.getPitch()));
        config.save();
    }
}
