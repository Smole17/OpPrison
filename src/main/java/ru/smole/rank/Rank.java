package ru.smole.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor public enum Rank {

    A("§7A", 0, 1),
    B("§7B", 2000, 2),
    C("§7C", 6000, 3),
    D("§7D", 18000, 4),
    E("§7E", 54000, 5),
    F("§7F", 160000, 6),
    G("§fG", 480000, 7),
    H("§fH", 900000, 8),
    I("§fI", 1800000, 9),
    J("§fJ", 3600000, 10),
    K("§fK", 6000000, 11),
    L("§fL", 8300000, 12),
    M("§2M", 13000000, 13),
    N("§2N", 20000000, 14),
    O("§2O", 30000000, 15),
    P("§2P", 43000000, 16),
    Q("§2Q", 58000000, 17),
    R("§aR", 87000000, 18),
    S("§aS", 114000000, 19),
    T("§aT", 130000000, 20),
    U("§aU", 158000000, 21),
    V("§9V", 183000000, 22),
    W("§9W", 230000000, 23),
    X("§9X", 283000000, 24),
    Y("§bY", 356000000, 25),
    Z("§b§nZ", 500000000, 26);

    private @Getter String name;
    private @Getter double cost;
    private @Getter int priority;
}
