package com.gabe.animalia.ml.dtos;

/**
 * Data Transfer Object representing the numerical state of a single fighter.
 * This is a "leaf node" in our state tree, containing only primitive data.
 */
public class FighterStateDTO {
    private String fighterId;
    private String name;
    private int currentHp;
    private int maxHp;
    private int currentEnergy;
    private String positionKey;
    private int flatDamageBuffTotal;
    private boolean isStunned;
    private boolean alive;



    public FighterStateDTO() {}

    public FighterStateDTO(String fighterId, String name, int currentHp, int maxHp, int currentEnergy, String positionKey, int flatDamageBuffTotal) {
        this.fighterId = fighterId;
        this.name = name;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.currentEnergy = currentEnergy;
        this.positionKey = positionKey;
        this.flatDamageBuffTotal = flatDamageBuffTotal;
        this.alive = currentHp > 0;
    }

    // Getters and Setters for Jackson
    public String getFighterId() { return fighterId; }
    public void setFighterId(String fighterId) { this.fighterId = fighterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getCurrentEnergy() { return currentEnergy; }
    public void setCurrentEnergy(int currentEnergy) { this.currentEnergy = currentEnergy; }
    public String getPositionKey() { return positionKey; }
    public void setPositionKey(String positionKey) { this.positionKey = positionKey; }
    public int getFlatDamageBuffTotal() { return flatDamageBuffTotal; }
    public void setFlatDamageBuffTotal(int flatDamageBuffTotal) { this.flatDamageBuffTotal = flatDamageBuffTotal; }
    public boolean isStunned() { return isStunned; }
    public void setStunned(boolean stunned) { isStunned = stunned; }
    public boolean isAlive() {return alive;}
    public void setAlive(boolean alive) {this.alive = alive;}
}
