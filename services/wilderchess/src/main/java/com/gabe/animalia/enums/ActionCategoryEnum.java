package com.gabe.animalia.enums;

public enum ActionCategoryEnum {
    NONE(0),
    ATTACK(1),
    BLOCK(2),
    SUPPORT(3),
    ITEM(4),
    PASSIVE(5),
    MOVE(6),
    BENCH(7);

    public final int id;

    ActionCategoryEnum(int id) {
        this.id = id;
    }
}
