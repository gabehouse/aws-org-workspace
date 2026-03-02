package com.gabe.animalia.ml.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Data Transfer Object (DTO) for all actions in the log (MOVE, ABILITY, etc.).
 * * This abstract class provides the universal identifying fields required by the
 * Machine Learning pipeline: WHO is acting (fighterId) and WHAT TYPE of action it is (type).
 * * NOTE: This is a pure DTO and contains no execution logic.
 */
public class LoggableActionDTO {

    private String subjectName;     // The fighter using the action (or "PLAYER" for items)
    private String type;            // {item, support, attack, block, move}
    private String name;            // The specific name (e.g., "Health Potion", "Slash")
    private String targetName; // Unified list of target IDs/Spots
    private double timeCost;
    private int energyCost;

    public LoggableActionDTO() {}

    /**
     * Full constructor for convenience in the Action.toLoggableDTO() method.
     */
    public LoggableActionDTO(String subjectName, String type, String name, String targetName, double timeCost, int energyCost) {
        this.subjectName = subjectName;
        this.type = type;
        this.name = name;
        this.targetName = targetName;
        this.timeCost = timeCost;
        this.energyCost = energyCost;
    }

    // Getters and Setters
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public double getTimeCost() { return timeCost; }
    public void setTimeCost(double timeCost) { this.timeCost = timeCost; }

    public int getEnergyCost() { return energyCost; }
    public void setEnergyCost(int energyCost) { this.energyCost = energyCost; }
}
