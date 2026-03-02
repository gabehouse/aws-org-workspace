package com.gabe.animalia.ml.game;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import java.util.Collections;
/**
 * The top-level wrapper class representing a single, complete game turn.
 * This object is the fundamental unit of data logged for ML training.
 */
public class GameTurnRecord {

    @JsonProperty("gameId")
    private String gameId;

    @JsonProperty("turnNumber")
    private int turnNumber;

    @JsonProperty("initialState")
    private TwoPlayerGameState initialState;

    @JsonProperty("finalState")
    private TwoPlayerGameState finalState;

    /**
     * Joint Action Space (A).
     * This list contains PlayerActionInput objects for all players involved in the turn.
     */
    @JsonProperty("jointActions")
    private List<PlayerActionInput> jointActions;

    /**
     * The reward/penalty vector (R) resulting from the joint actions.
     */
    @JsonProperty("outcome")
    private GameTurnOutcome outcome;

    /**
     * Required for JSON deserialization (Jackson, Firestore, etc).
     */
    public GameTurnRecord() {}

    /**
     * Primary constructor used by TurnOrchestrator.
     * By passing the full TwoPlayerGameState objects, the resulting JSON
     * will be fully nested.
     */
    public GameTurnRecord(
        String gameId,
        int turnNumber,
        TwoPlayerGameState initialState,
        TwoPlayerGameState finalState,
        List<PlayerActionInput> jointActions,
        GameTurnOutcome outcome
    ) {
        this.gameId = gameId;
        this.turnNumber = turnNumber;
        this.initialState = initialState;
        this.finalState = finalState;
        this.jointActions = jointActions != null ? List.copyOf(jointActions) : Collections.emptyList();
        this.outcome = outcome;
    }

    // Getters and Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public TwoPlayerGameState getInitialState() { return initialState; }
    public void setInitialState(TwoPlayerGameState initialState) { this.initialState = initialState; }

    public TwoPlayerGameState getFinalState() { return finalState; }
    public void setFinalState(TwoPlayerGameState finalState) { this.finalState = finalState; }

    public List<PlayerActionInput> getJointActions() { return jointActions; }
    public void setJointActions(List<PlayerActionInput> jointActions) { this.jointActions = jointActions; }

    public GameTurnOutcome getOutcome() { return outcome; }
    public void setOutcome(GameTurnOutcome outcome) { this.outcome = outcome; }
}
