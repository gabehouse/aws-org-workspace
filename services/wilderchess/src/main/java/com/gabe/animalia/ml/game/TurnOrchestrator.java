package com.gabe.animalia.ml.game;

import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import com.gabe.animalia.ml.dtos.PlayerStateDTO;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.ml.server.GameLogger;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class demonstrates the "Next Step": Integrating the Logger into the game loop.
 */
public class TurnOrchestrator {

    private final GameLogger logger;
    private final GameSessionLog session;
    private TwoPlayerGameState currentInitialState;

    public TurnOrchestrator(String gameId, GameLogger logger) {
        this.logger = logger;
        this.session = new GameSessionLog(gameId);
    }

    /**
     * Captures the state at the start of the turn (S).
     */
    public TwoPlayerGameState captureInitialState(int turnNumber, Player p1, Player p2) {
        TwoPlayerGameState stateS = new TwoPlayerGameState(
            turnNumber,
            p1.toStateDto(),
            p2.toStateDto()
        );

        this.currentInitialState = stateS;
        return stateS;
    }

    /**
     * Records the transition from S to S' via actions A and outcome R.
     */
    public void recordTurnTransition(
        int turnNumber,
        TwoPlayerGameState initialState,
        Player p1,
        Player p2,
        List<LoggableActionDTO> p1Actions,
        List<LoggableActionDTO> p2Actions,
        int p1HealthDelta,
        int p1EnergyDelta,
        int p2HealthDelta,
        int p2EnergyDelta
    ) {
        TwoPlayerGameState stateSPrime = new TwoPlayerGameState(
            turnNumber,
            p1.toStateDto(),
            p2.toStateDto()
        );

        GameTurnOutcome outcome = new GameTurnOutcome(
            p1HealthDelta, p1EnergyDelta,
            p2HealthDelta, p2EnergyDelta
        );

        PlayerActionInput input1 = new PlayerActionInput(p1.getId(), p1Actions);
        PlayerActionInput input2 = new PlayerActionInput(p2.getId(), p2Actions);
        List<PlayerActionInput> allInputs = Arrays.asList(input1, input2);

        GameTurnRecord record = new GameTurnRecord(
            session.getGameId(),
            turnNumber,
            initialState,
            stateSPrime,
            allInputs,
            outcome
        );

        session.addTurn(record);
        // Clear the initial state tracking since this turn is now complete
        this.currentInitialState = null;
    }

    /**
     * Finalizes a successful match.
     */
    public void recordGameOutcome(String winningPlayerId, int totalTurns) {
        saveFinalSession(winningPlayerId, totalTurns, false, null);
    }

    /**
     * Emergency save if the game crashes or an error is thrown.
     * Preserves all turns recorded up to this point.
     * * @param totalTurns The turn reached before the crash.
     * @param errorMsg The exception message or stack trace.
     */
    public void recordEmergencySave(int totalTurns, String errorMsg) {
        // If the crash happened during a turn (after captureInitialState),
        // we add a partial turn record so you can see the state at the moment of failure.
        if (currentInitialState != null) {
            GameTurnRecord partialRecord = new GameTurnRecord(
                session.getGameId(),
                totalTurns,
                currentInitialState,
                null, // No final state because the turn crashed
                null, // No actions recorded
                null  // No outcome calculated
            );
            session.addTurn(partialRecord);
        }

        saveFinalSession("ERROR", totalTurns, true, errorMsg);
    }

    /**
     * Shared logic for finalizing the JSON file.
     */
    private void saveFinalSession(String winner, int turns, boolean isError, String errorMsg) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        // Use a prefix to make error logs stand out in the file system/S3 bucket
        String prefix = isError ? "CRASH_" : "";
        String logFileName = prefix + timestamp + "_" + session.getGameId();

        GameFinalResult result = new GameFinalResult(session.getGameId(), winner, turns, timestamp);

        // Populate error fields for debugging and automated data cleaning
        result.setError(isError);
        result.setErrorMessage(errorMsg);

        session.setFinalResult(result);

        // The 'session' object contains the full 'turns' list, preserving history
        logger.saveGameSession(session, logFileName);
    }

    public TwoPlayerGameState getCurrentInitialState() {
        return currentInitialState;
    }
}
