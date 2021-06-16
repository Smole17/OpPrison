package ru.smole.data.prices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.smole.data.items.pickaxe.Upgrade;

public class PricesManager {

    public PricesManager() {
    }

    public double getPrice(String price) {
        Prices prices = getPriceFromString(price);
        return prices == null ? 0 : prices.getCost();
    }

    public Prices getPriceFromString(String price) {
        for (Prices prices : Prices.values()) {
            if (prices == Prices.valueOf(price)) {
                return prices;
            }
        }

        return null;
    }

    @AllArgsConstructor
    public enum Prices {

        DIRT(Material.DIRT, 1),
        SAND(Material.SAND, 2),
        GRAVEL(Material.GRAVEL, 3);

        private @Getter Material material;
        private @Getter double cost;
    }
}
