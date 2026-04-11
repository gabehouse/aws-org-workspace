package com.gabe.animalia.enums;

public enum TargetTypeEnum {
    NONE(0), // Passive/Global
    FIGHTER(1), // Single Target (Enemy or Ally)
    SQUARE(2), // AOE or Location based
    SELF(3); // Buffs/Self-heals

    public final int id;

    TargetTypeEnum(int id) {
        this.id = id;
    }
}
