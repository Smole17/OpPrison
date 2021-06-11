package ru.smole.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor public enum Rank {

    A("§7A", "A", 0, 1),
    B("§7B", "B", 2000, 2),
    C("§7C", "C", 6000, 3),
    D("§7D", "D", 18000, 4),
    E("§7E", "E", 54000, 5),
    F("§7F", "F", 160000, 6),
    G("§fG", "G", 480000, 7),
    H("§fH", "H", 900000, 8),
    I("§fI", "I", 1800000, 9),
    J("§fJ", "J", 3600000, 10),
    K("§fK", "K", 6000000, 11),
    L("§fL", "L", 8300000, 12),
    M("§2M", "M", 13000000, 13),
    N("§2N", "N", 20000000, 14),
    O("§2O", "O", 30000000, 15),
    P("§2P", "P", 43000000, 16),
    Q("§2Q", "Q", 58000000, 17),
    R("§aR", "R", 87000000, 18),
    S("§aS", "S", 114000000, 19),
    T("§aT", "T", 130000000, 20),
    U("§aU", "U", 158000000, 21),
    V("§9V", "V", 183000000, 22),
    W("§9W", "W", 230000000, 23),
    X("§9X", "X", 283000000, 24),
    Y("§bY", "Y", 356000000, 25),
    Z("§b§nZ", "Z", 500000000, 26);

    private @Getter String name;
    private @Getter String clearName;
    private @Getter double cost;
    private @Getter int priority;
}
