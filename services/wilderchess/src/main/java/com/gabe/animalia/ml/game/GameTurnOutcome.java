package com.gabe.animalia.ml.game;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * Represents the measurable reward features (R) of the turn.
 * Minimal version used for initial verification.
 */
public class GameTurnOutcome {

    @JsonProperty("p1_hp_delta")
    private int p1HealthDelta;

    @JsonProperty("p1_en_delta")
    private int p1EnergyDelta;

    @JsonProperty("p2_hp_delta")
    private int p2HealthDelta;

    @JsonProperty("p2_en_delta")
    private int p2EnergyDelta;

    public GameTurnOutcome() {}

    public GameTurnOutcome(int p1HealthDelta, int p1EnergyDelta, int p2HealthDelta, int p2EnergyDelta) {
        this.p1HealthDelta = p1HealthDelta;
        this.p1EnergyDelta = p1EnergyDelta;
        this.p2HealthDelta = p2HealthDelta;
        this.p2EnergyDelta = p2EnergyDelta;
    }

    // Getters and Setters
    public int getP1HealthDelta() { return p1HealthDelta; }
    public void setP1HealthDelta(int p1HealthDelta) { this.p1HealthDelta = p1HealthDelta; }
    public int getP1EnergyDelta() { return p1EnergyDelta; }
    public void setP1EnergyDelta(int p1EnergyDelta) { this.p1EnergyDelta = p1EnergyDelta; }
    public int getP2HealthDelta() { return p2HealthDelta; }
    public void setP2HealthDelta(int p2HealthDelta) { this.p2HealthDelta = p2HealthDelta; }
    public int getP2EnergyDelta() { return p2EnergyDelta; }
    public void setP2EnergyDelta(int p2EnergyDelta) { this.p2EnergyDelta = p2EnergyDelta; }
}
