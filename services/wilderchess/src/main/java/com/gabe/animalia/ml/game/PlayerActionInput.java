package com.gabe.animalia.ml.game;

import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import java.util.List;

/** * POJO representing the input bundle submitted by a single player for one turn.
 * This object is used in the GameTurnRecord to hold the Action (A) component.
 */
public class PlayerActionInput {

    // The unique ID of the player who submitted the actions
    private int playerId;

    // The sequence of actions the player intends to take (using the minimal DTOs)
    private List<LoggableActionDTO> actions;

    public PlayerActionInput() {}

    public PlayerActionInput(int playerId, List<LoggableActionDTO> actions) {
        this.playerId = playerId;
        this.actions = actions;
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public List<LoggableActionDTO> getActions() { return actions; }
    public void setActions(List<LoggableActionDTO> actions) { this.actions = actions; }
}
