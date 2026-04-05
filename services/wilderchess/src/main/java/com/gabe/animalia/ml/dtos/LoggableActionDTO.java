package com.gabe.animalia.ml.dtos;

import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.enums.TargetTypeEnum;

/**
 * Base Data Transfer Object (DTO) for all actions in the log (MOVE, ABILITY,
 * etc.).
 * * This abstract class provides the universal identifying fields required by
 * the
 * Machine Learning pipeline: WHO is acting (fighterId) and WHAT TYPE of action
 * it is (type).
 * * NOTE: This is a pure DTO and contains no execution logic.
 */
public class LoggableActionDTO {

    // Identity Fields
    private int subjectId; // ID of the acting fighter
    private int subjectType; // Unit type (e.g., Wolf, Bear)
    private String subjectName;

    // Action Identity
    private int abilityId; // The unique ID from ActionEnum
    private String name; // e.g., "THIEF_GLOVES"
    private ActionCategoryEnum type; // item, attack, etc.

    // Targeting Info
    private int targetId;
    private String targetName;
    private String targetPos;
    private TargetTypeEnum targetType;

    // Numerical Features (Critical for ML Training)
    private double timeCost;
    private int energyCost;
    private int damage;
    private int healing;
    private int block;
    private int duration;
    private int statusValue;
    private int energyRestore;
    private boolean stackable;
    private StatusEnum status;
    private ActionCategoryEnum category;

    /**
     * Full constructor for convenience in the Action.toLoggableDTO() method.
     */
    public LoggableActionDTO(int subjectId, int subjectType, String subjectName,
            ActionEnum action, int targetId, String targetPos,
            String targetName) {
        this.subjectId = subjectId;
        this.subjectType = subjectType;
        this.subjectName = subjectName;

        // Mapping from Enum
        this.abilityId = action.id;
        this.name = action.name();
        this.type = action.category;
        this.timeCost = action.time;
        this.energyCost = action.energy;
        this.category = action.category; // Mapping the Enum (ATTACK, BLOCK, etc.)
        this.status = action.status;

        // Combat Stats
        this.damage = action.damage;
        this.healing = action.healing;
        this.block = action.block; // Added
        this.energyRestore = action.energyRestore; // Added

        // Status & Logic
        this.duration = action.duration;
        this.statusValue = action.statusValue;
        this.stackable = action.stackable; // Added (1 for true, 0 for false in CSV)

        // Targeting
        this.targetId = targetId;
        this.targetPos = targetPos;
        this.targetName = targetName;
        this.targetType = action.targetType;
    }
    // Getters and Setters

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public ActionCategoryEnum getCategory() {
        return category;
    }

    public void setCategory(ActionCategoryEnum category) {
        this.category = category;
    }

    public int getEnergyRestore() {
        return energyRestore;
    }

    public void setEnergyRestore(int energyRestore) {
        this.energyRestore = energyRestore;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public LoggableActionDTO() {
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getHealing() {
        return healing;
    }

    public void setHealing(int healing) {
        this.healing = healing;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(int statusValue) {
        this.statusValue = statusValue;
    }

    public TargetTypeEnum getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetTypeEnum targetType) {
        this.targetType = targetType;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(int abilityId) {
        this.abilityId = abilityId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(int subjectType) {
        this.subjectType = subjectType;
    }

    public String getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(String targetPos) {
        this.targetPos = targetPos;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public ActionCategoryEnum getType() {
        return type;
    }

    public void setType(ActionCategoryEnum type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public double getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(double timeCost) {
        this.timeCost = timeCost;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }
}
