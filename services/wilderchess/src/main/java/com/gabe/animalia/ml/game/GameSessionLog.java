package com.gabe.animalia.ml.game;

import java.util.List;
import java.util.ArrayList;

/**
 * A complete, self-contained log of a single game session.
 * This is the ultimate "Training Example" for an ML model.
 */
public class GameSessionLog {
    private String gameId;
    private List<GameTurnRecord> turns = new ArrayList<>();
    private GameFinalResult finalResult;

    public GameSessionLog() {}

    public GameSessionLog(String gameId) {
        this.gameId = gameId;
    }

    public void addTurn(GameTurnRecord turn) {
        this.turns.add(turn);
    }

    // Getters and Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public List<GameTurnRecord> getTurns() { return turns; }
    public void setTurns(List<GameTurnRecord> turns) { this.turns = turns; }

    public GameFinalResult getFinalResult() { return finalResult; }
    public void setFinalResult(GameFinalResult finalResult) { this.finalResult = finalResult; }
}
