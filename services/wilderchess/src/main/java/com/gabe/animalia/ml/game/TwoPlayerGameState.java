package com.gabe.animalia.ml.game;

import com.gabe.animalia.ml.dtos.PlayerStateDTO;

/**
 * The combined state of both players at a specific turn.
 * This is the "heavy" object stored in the state log files.
 */
public class TwoPlayerGameState {
    private int turnNumber;
    private PlayerStateDTO playerA;
    private PlayerStateDTO playerB;

    public TwoPlayerGameState() {}

    public TwoPlayerGameState(int turnNumber, PlayerStateDTO playerA, PlayerStateDTO playerB) {
        this.turnNumber = turnNumber;
        this.playerA = playerA;
        this.playerB = playerB;
    }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }
    public PlayerStateDTO getPlayerA() { return playerA; }
    public void setPlayerA(PlayerStateDTO playerA) { this.playerA = playerA; }
    public PlayerStateDTO getPlayerB() { return playerB; }
    public void setPlayerB(PlayerStateDTO playerB) { this.playerB = playerB; }
}
