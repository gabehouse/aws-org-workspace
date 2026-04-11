package com.gabe.animalia.enums;

public enum FighterType {
    NONE(0, "None"),
    LION(1, "Lion"),
    DONKEY(2, "Donkey"),
    NEWT(3, "Newt"),
    WOLF(4, "Wolf"),
    FOX(5, "Fox"),
    BULL(6, "Bull"),
    TURTLE(7, "Turtle"),
    DOVE(8, "Dove"),
    PIG(9, "Pig"),
    HERON(10, "Heron"),
    BAT(11, "Bat"),
    HAWK(12, "Hawk");

    public final int id;
    public final String name;

    FighterType(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
