package com.gabe.animalia.ml.game;

/**
 * Represents the final, immutable label of the entire game (L).
 * Minimal version used for initial verification.
 */
public class GameFinalResult {

    private String gameId;
    private String winningPlayerId;
    private int totalTurns;
    private String timestamp;

    // New fields for debugging and data filtering
    private boolean isError = false;
    private String errorMessage;

    public GameFinalResult() {}

    public GameFinalResult(String gameId, String winningPlayerId, int totalTurns, String timestamp) {
        this.gameId = gameId;
        this.winningPlayerId = winningPlayerId;
        this.totalTurns = totalTurns;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getWinningPlayerId() { return winningPlayerId; }
    public void setWinningPlayerId(String winningPlayerId) { this.winningPlayerId = winningPlayerId; }

    public int getTotalTurns() { return totalTurns; }
    public void setTotalTurns(int totalTurns) { this.totalTurns = totalTurns; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isError() { return isError; }
    public void setError(boolean error) { isError = error; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
