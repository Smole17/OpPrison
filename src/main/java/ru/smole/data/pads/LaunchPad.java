package ru.smole.data.pads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.smole.utils.config.ConfigUtils;

@Getter
public class LaunchPad {

    private final Location location;
    private final Vector vector;

    public LaunchPad(ConfigurationSection section) {
        this.location = ConfigUtils.loadLocationFromConfigurationSectionSplit(section);
        this.vector = loadVector(section);
        location.getBlock().setType(Material.IRON_PLATE);
    }

    public void launch(Player player) {
        player.setVelocity(vector);
        location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 1);
        player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
    }

    protected Vector loadVector(ConfigurationSection section) {
        String[] split = section.getString("velocity").split(" ");
        return location.getDirection()
                .multiply(Double.parseDouble(split[0]))
                .setY(Double.parseDouble(split[1]));
    }
}
