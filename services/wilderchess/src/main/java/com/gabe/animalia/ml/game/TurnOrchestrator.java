package com.gabe.animalia.ml.game;

import com.gabe.animalia.ml.dtos.FighterStateDTO;
import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import com.gabe.animalia.ml.dtos.PlayerStateDTO;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.ml.server.GameLogger;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class demonstrates the "Next Step": Integrating the Logger into the game
 * loop.
 */
public class TurnOrchestrator {

    private final GameLogger logger;
    private GameSessionLog session;
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
                p2.toStateDto());

        this.currentInitialState = stateS;
        return stateS;
    }

    public void newGame() {
        this.session = new GameSessionLog(UUID.randomUUID().toString());
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
            List<LoggableActionDTO> p2Actions) {

        // 1. Create the Final State snapshot first
        TwoPlayerGameState stateSPrime = new TwoPlayerGameState(
                turnNumber,
                p1.toStateDto(),
                p2.toStateDto());

        // 2. Derive deltas directly from the two snapshots
        // This ensures that Delta = State(S') - State(S) exactly.
        int p1HealthDelta = sumHealth(stateSPrime.getPlayerA()) - sumHealth(initialState.getPlayerA());
        int p1EnergyDelta = sumEnergy(stateSPrime.getPlayerA()) - sumEnergy(initialState.getPlayerA());

        int p2HealthDelta = sumHealth(stateSPrime.getPlayerB()) - sumHealth(initialState.getPlayerB());
        int p2EnergyDelta = sumEnergy(stateSPrime.getPlayerB()) - sumEnergy(initialState.getPlayerB());

        GameTurnOutcome outcome = new GameTurnOutcome(
                p1HealthDelta, p1EnergyDelta,
                p2HealthDelta, p2EnergyDelta);

        // 3. Log the record as before
        PlayerActionInput input1 = new PlayerActionInput(p1.getId(), p1Actions);
        PlayerActionInput input2 = new PlayerActionInput(p2.getId(), p2Actions);

        GameTurnRecord record = new GameTurnRecord(
                session.getGameId(),
                turnNumber,
                initialState,
                stateSPrime,
                Arrays.asList(input1, input2),
                outcome);

        session.addTurn(record);
        this.currentInitialState = null;
    }

    // Helper to sum health from a PlayerStateDTO
    private int sumHealth(PlayerStateDTO player) {
        return player.getFighters().stream().mapToInt(FighterStateDTO::getCurrentHp).sum();
    }

    private int sumEnergy(PlayerStateDTO player) {
        return player.getFighters().stream().mapToInt(FighterStateDTO::getCurrentEnergy).sum();
    }

    /**
     * Finalizes a successful match.
     */
    public void recordGameOutcome(int winningPlayerId, int totalTurns) {
        saveFinalSession(winningPlayerId, totalTurns, false, null);
    }

    /**
     * Emergency save if the game crashes or an error is thrown.
     * Preserves all turns recorded up to this point.
     * * @param totalTurns The turn reached before the crash.
     *
     * @param errorMsg The exception message or stack trace.
     */
    public void recordEmergencySave(int totalTurns, String errorMsg) {
        // If the crash happened during a turn (after captureInitialState),
        // we add a partial turn record so you can see the state at the moment of
        // failure.
        if (currentInitialState != null) {
            GameTurnRecord partialRecord = new GameTurnRecord(
                    session.getGameId(),
                    totalTurns,
                    currentInitialState,
                    null, // No final state because the turn crashed
                    null, // No actions recorded
                    null // No outcome calculated
            );
            session.addTurn(partialRecord);
        }

        saveFinalSession(-2, totalTurns, true, errorMsg);
    }

    /**
     * Shared logic for finalizing the JSON file.
     */
    private void saveFinalSession(int winner, int turns, boolean isError, String errorMsg) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String prefix = isError ? "CRASH_" : "SUCCESS_"; // Using underscore instead of $ for cleaner filenames

        // 1. Prepare the result object
        GameFinalResult result = new GameFinalResult(session.getGameId(), winner, turns, timestamp);
        result.setError(isError);
        result.setErrorMessage(errorMsg);
        session.setFinalResult(result);

        // 2. Prepare the identifier (Suffix for the gameId)
        String statusAndTimestamp = prefix + timestamp;

        // 3. SAVE JSON (Goes to gamelogs/ as a flat file)
        // logger.saveGameSession(session, statusAndTimestamp);

        // 4. SAVE CSV (Goes to ml_data/ as a flat file)
        GameFeaturizer featurizer = new GameFeaturizer();
        List<String> csvRows = featurizer.featurizeSession(session);

        logger.saveToCsvFile(session.getGameId(), statusAndTimestamp, csvRows);
    }

    public TwoPlayerGameState getCurrentInitialState() {
        return currentInitialState;
    }
}
