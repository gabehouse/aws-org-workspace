package com.gabe.animalia.ml.dtos;

import com.gabe.animalia.enums.FighterType;

/**
 * Data Transfer Object representing the numerical state of a single fighter.
 * This is a "leaf node" in our state tree, containing only primitive data.
 */

public class FighterStateDTO {
    private int id; // The instance (e.g., "3")
    private int typeId; // The AI's number (e.g., 1 for Lion)
    private String name; // For your logs (e.g., "Lion")
    private int currentHp;
    private int maxHp;
    private int currentEnergy;
    private String positionKey; // Universal key (e.g., "TopFront")
    private int flatDamageBuffTotal;
    private boolean alive;
    private boolean isStunned;
    private int spot;

    public FighterStateDTO() {
    }

    public FighterStateDTO(int id, FighterType type, int currentHp, int maxHp,
            int currentEnergy, int spot, int flatDamageBuffTotal,
            boolean alive, boolean isStunned) {
        this.id = id;

        // Use the Enum to fill both TypeID and Name automatically
        FighterType effectiveType = (type != null) ? type : FighterType.NONE;
        this.typeId = effectiveType.id;
        this.name = effectiveType.name;

        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.currentEnergy = currentEnergy;
        this.spot = spot;
        this.flatDamageBuffTotal = flatDamageBuffTotal;
        this.alive = alive;
        this.isStunned = isStunned;
    }

    public static FighterStateDTO empty(int spotIndex) {
        // Pass '0' for the ID, NONE for the type, and the numerical index for the spot
        return new FighterStateDTO(
                0,
                FighterType.NONE,
                0, 0, 0,
                spotIndex, // Now an int
                0,
                false,
                false);
    }

    // Getters and Setters for Jackson
    public int getSpot() {
        return spot;
    }

    public void setSpot(int spot) {
        this.spot = spot;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public void setCurrentEnergy(int currentEnergy) {
        this.currentEnergy = currentEnergy;
    }

    public String getPositionKey() {
        return positionKey;
    }

    public void setPositionKey(String positionKey) {
        this.positionKey = positionKey;
    }

    public int getFlatDamageBuffTotal() {
        return flatDamageBuffTotal;
    }

    public void setFlatDamageBuffTotal(int flatDamageBuffTotal) {
        this.flatDamageBuffTotal = flatDamageBuffTotal;
    }

    public boolean isStunned() {
        return isStunned;
    }

    public void setStunned(boolean stunned) {
        isStunned = stunned;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
