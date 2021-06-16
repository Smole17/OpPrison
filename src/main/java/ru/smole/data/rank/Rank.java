package ru.smole.data.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor public enum Rank {

    A("§7A", 0),
    B("§7B", 2000),
    C("§7C", 6000),
    D("§7D", 18000),
    E("§7E", 54000),
    F("§7F", 160000),
    G("§fG", 480000),
    H("§fH", 900000),
    I("§fI", 1800000),
    J("§fJ", 3600000),
    K("§fK", 6000000),
    L("§fL", 8300000),
    M("§2M", 13000000),
    N("§2N", 20000000),
    O("§2O", 30000000),
    P("§2P", 43000000),
    Q("§2Q", 58000000),
    R("§aR", 87000000),
    S("§aS", 114000000),
    T("§aT", 130000000),
    U("§aU", 158000000),
    V("§9V", 183000000),
    W("§9W", 230000000),
    X("§9X", 283000000),
    Y("§bY", 356000000),
    Z("§b§nZ", 500000000);

    private @Getter String name;
    private @Getter double cost;
}
