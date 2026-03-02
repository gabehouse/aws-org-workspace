package com.gabe.animalia.ml.dtos;

import java.util.List;

/**
 * Data Transfer Object representing a player's high-level state.
 * Refined to include only the four essential fields for the ML model.
 */
public class PlayerStateDTO {
    private String id;
    private int morale;
    private List<FighterStateDTO> fighters;
    private List<String> items;

    public PlayerStateDTO() {}

    public PlayerStateDTO(String id, int morale, List<FighterStateDTO> fighters, List<String> items) {
        this.id = id;
        this.morale = morale;
        this.fighters = fighters;
        this.items = items;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getMorale() { return morale; }
    public void setMorale(int morale) { this.morale = morale; }

    public List<FighterStateDTO> getFighters() { return fighters; }
    public void setFighters(List<FighterStateDTO> fighters) { this.fighters = fighters; }

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}
