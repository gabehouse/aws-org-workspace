package com.gabe.animalia.enums;

public enum StatusEnum {
    NONE(0, "none"),
    BURN(1, "burn"),
    BLOCK(2, "block"),
    STEALTH(3, "stealth"),
    STUN(4, "stun"),
    BUFF(5, "buff"),
    CURSE(6, "curse"),
    ROOT(7, "root"),
    CHANNELLING(8, "channelling");

    public final int id;
    public final String label;

    StatusEnum(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static StatusEnum fromId(int id) {
        for (StatusEnum s : values()) {
            if (s.id == id)
                return s;
        }
        return NONE;
    }
}
