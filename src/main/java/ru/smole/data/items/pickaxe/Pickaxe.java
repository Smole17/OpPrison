package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data public class Pickaxe {

    public Player player;
    public String name;
    public double exp;
    public double level;
    public List<Map<Upgrade, Double>> upgrades;

    public double getNextExp() {
        return exp + (exp * 0.1);
    }
}
