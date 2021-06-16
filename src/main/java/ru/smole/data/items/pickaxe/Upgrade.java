package ru.smole.data.items.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor public enum Upgrade {

    EFFICIENCY("§7Эффективность", 0,10, 300,  20000),
    FORTUNE("§7Удача", 0,15, 500000, 680),
    TOKEN_MINER("§eУдача на токены", 0, 100, 5000, 2500000),
    KEY_FINDER("§4Удача на ключи", 2, 0, 50, 1000000000),
    EXPLOSIVE("§4Взрыв", 5, 0, 500, 100000000),
    HASTE("§eСпешка", 1,10, 5, 25000),
    SPEED("§fСкорость", 1,10, 5, 25000),
    JUMP_BOOST("§aПрыгучесть", 1,10, 5, 25000),
    NIGHT_VISION("§5Ночное зрение", 1, 0, 1, 25000);

    private @Getter String name;
    private @Getter double need_level_pickaxe;
    private @Getter double start_level;
    private @Getter double max_level;
    private @Getter double start_cost;
}
