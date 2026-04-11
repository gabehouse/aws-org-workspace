package com.gabe.animalia.ml.game;

import java.util.*;
import java.util.stream.Collectors;

import com.gabe.animalia.ml.dtos.FighterStateDTO;
import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import com.gabe.animalia.ml.dtos.PlayerStateDTO;

public class GameFeaturizer {
    private static final int FIGHTER_COUNT = 4;
    private static final int MAX_ACTIONS_PER_PLAYER = 10;

    public float[] featurizeForInference(int turnNumber, PlayerStateDTO pA, PlayerStateDTO pB,
            List<PlayerActionInput> jointActions) {
        List<String> features = new ArrayList<>();

        // 1. TURN
        features.add(String.valueOf(turnNumber));

        // 2. INITIAL STATES (50 fields)
        appendPlayerState(features, pA);
        appendPlayerState(features, pB);

        // 3. THE PROPOSED ACTIONS (300 fields)
        appendPlayerActions(features, jointActions, 0);
        appendPlayerActions(features, jointActions, 1);

        // DO NOT append Final States here. The model's job is to
        // predict the final state/win chance based on the above.

        float[] featureArray = new float[features.size()];
        for (int i = 0; i < features.size(); i++) {
            featureArray[i] = Float.parseFloat(features.get(i));
        }

        return featureArray; // This will be exactly 351
    }

    /**
     * Converts a full session into a list of CSV strings.
     * Every row will now include the final result label.
     */
    public List<String> featurizeSession(GameSessionLog session) {
        GameFinalResult finalResult = session.getFinalResult();

        // Skip sessions that didn't finish properly unless you want to learn from
        // crashes
        if (finalResult == null || finalResult.isError()) {
            return Collections.emptyList();
        }

        return session.getTurns().stream()
                .filter(turn -> turn.getOutcome() != null) // Ignore partial/crashed turns
                .map(turn -> featurizeTurn(turn, finalResult))
                .collect(Collectors.toList());
    }

    private String featurizeTurn(GameTurnRecord turn, GameFinalResult result) {
        List<String> features = new ArrayList<>();

        // 1. TURNS & INITIAL STATE
        features.add(String.valueOf(turn.getTurnNumber()));
        appendPlayerState(features, turn.getInitialState().getPlayerA());
        appendPlayerState(features, turn.getInitialState().getPlayerB());

        // 2. THE INPUT (ACTIONS)
        appendPlayerActions(features, turn.getJointActions(), 0);
        appendPlayerActions(features, turn.getJointActions(), 1);

        // 3. FINAL STATE
        appendPlayerState(features, turn.getFinalState().getPlayerA());
        appendPlayerState(features, turn.getFinalState().getPlayerB());

        // 4. DERIVE DELTAS FROM THE SNAPSHOTS (Not from turn.getOutcome())
        // Using the snapshots guarantees that Initial + Delta = Final
        int p1HealthFinal = sumHealth(turn.getFinalState().getPlayerA());
        int p1HealthInitial = sumHealth(turn.getInitialState().getPlayerA());
        int p2HealthFinal = sumHealth(turn.getFinalState().getPlayerB());
        int p2HealthInitial = sumHealth(turn.getInitialState().getPlayerB());

        int p1EnergyFinal = sumEnergy(turn.getFinalState().getPlayerA());
        int p1EnergyInitial = sumEnergy(turn.getInitialState().getPlayerA());
        int p2EnergyFinal = sumEnergy(turn.getFinalState().getPlayerB());
        int p2EnergyInitial = sumEnergy(turn.getInitialState().getPlayerB());

        float p1MaxHP = calculateTeamMaxHP(turn.getInitialState().getPlayerA());
        float p2MaxHP = calculateTeamMaxHP(turn.getInitialState().getPlayerB());
        float p1MaxEN = calculateTeamMaxEnergy(turn.getInitialState().getPlayerA());
        float p2MaxEN = calculateTeamMaxEnergy(turn.getInitialState().getPlayerB());

        // 5. THE LABELS
        // Normalized Deltas
        features.add(String.format("%.4f", (float) (p1HealthFinal - p1HealthInitial) / p1MaxHP));
        features.add(String.format("%.4f", (float) (p2HealthFinal - p2HealthInitial) / p2MaxHP));
        features.add(String.format("%.4f", (float) (p1EnergyFinal - p1EnergyInitial) / p1MaxEN));
        features.add(String.format("%.4f", (float) (p2EnergyFinal - p2EnergyInitial) / p2MaxEN));

        // Long-term: The Winner
        double winnerLabel = result.getWinningPlayerId();
        features.add(String.format("%.1f", winnerLabel));

        return String.join(",", features);
    }

    // Helper methods to extract data from the DTOs
    private int sumHealth(PlayerStateDTO player) {
        return player.getFighters().stream().mapToInt(FighterStateDTO::getCurrentHp).sum();
    }

    private int sumEnergy(PlayerStateDTO player) {
        return player.getFighters().stream().mapToInt(FighterStateDTO::getCurrentEnergy).sum();
    }

    private float calculateTeamMaxHP(PlayerStateDTO player) {
        if (player == null || player.getFighters() == null)
            return 1.0f;
        // Change return to 1.0f if sum is 0 to avoid NaN
        return Math.max(1.0f, (float) player.getFighters().stream().mapToDouble(f -> f.getMaxHp()).sum());
    }

    private float calculateTeamMaxEnergy(PlayerStateDTO player) {
        if (player == null || player.getFighters() == null)
            return 1.0f;
        // Change return to 1.0f if sum is 0 to avoid NaN
        return Math.max(1.0f, (float) player.getFighters().stream().mapToDouble(f -> 100).sum());
    }

    private void appendPlayerState(List<String> features, PlayerStateDTO player) {
        // 1. Morale (Static column index 0 for player state)
        features.add(String.valueOf(player.getMorale()));

        List<FighterStateDTO> fighters = player.getFighters();

        // Loop through 4 FIXED SLOTS.
        // CRITICAL: Ensure player.getFighters() returns all 4 units (living or dead)
        // in the same order every turn.
        for (int i = 0; i < 4; i++) {
            if (i < fighters.size()) {
                FighterStateDTO f = fighters.get(i);

                features.add(String.valueOf(f.getTypeId()));
                features.add(String.valueOf(f.getSpot()));

                // FIX: Clamp HP between 0 and 1. Avoids negative outliers.
                double hpRatio = f.getMaxHp() > 0 ? f.getCurrentHp() / (double) f.getMaxHp() : 0.0;
                features.add(String.format("%.4f", Math.max(0.0, Math.min(1.0, hpRatio))));

                features.add(String.format("%.4f", f.getCurrentEnergy() / 100.0));
                features.add(f.isAlive() ? "1" : "0");
                features.add(f.isStunned() ? "1" : "0");
            } else {
                // Padding for empty roster slots (6 fields)
                // This only hits if a player literally started with < 4 units.
                for (int j = 0; j < 6; j++) {
                    features.add("0");
                }
            }
        }
    }

    private void appendPlayerActions(List<String> features, List<PlayerActionInput> jointActions, int playerId) {
        List<LoggableActionDTO> actions = jointActions.stream()
                .filter(input -> input.getPlayerId() == playerId)
                .findFirst()
                .map(PlayerActionInput::getActions)
                .orElse(Collections.emptyList());

        for (int i = 0; i < MAX_ACTIONS_PER_PLAYER; i++) {
            if (i < actions.size()) {
                appendSingleAction(features, actions.get(i));
            } else {
                // This now dynamically stays in sync with your action size
                padEmptyAction(features);
            }
        }
    }

    private void appendSingleAction(List<String> features, LoggableActionDTO a) {
        features.add(String.valueOf(a.getSubjectId()));
        features.add(String.valueOf(a.getTargetId()));
        features.add(String.valueOf(a.getAbilityId()));
        features.add(String.valueOf(a.getCategory().ordinal()));
        features.add(String.valueOf(a.getStatus().ordinal())); // Field 5
        features.add(String.format("%.4f", a.getTimeCost() / 10.0));
        features.add(String.format("%.4f", a.getEnergyCost() / 100.0));
        features.add(String.format("%.4f", a.getDamage() / 100.0));
        features.add(String.format("%.4f", a.getHealing() / 100.0));
        features.add(String.format("%.4f", a.getBlock() / 100.0));
        features.add(String.format("%.4f", a.getEnergyRestore() / 100.0));
        features.add(String.format("%.4f", a.getStatusValue() / 10.0));

        int duration = a.getDuration();
        features.add(duration >= 900 ? "1.0000" : String.format("%.4f", Math.min(duration, 10) / 10.0));

        features.add(a.isStackable() ? "1" : "0");
        features.add(String.valueOf(a.getTargetType().ordinal()));
    }

    private void padEmptyAction(List<String> features) {
        // There are exactly 15 fields in appendSingleAction.
        // If you add one there, add one "0" here.
        for (int j = 0; j < 15; j++) {
            features.add("0");
        }
    }

    public String getCsvHeader() {
        List<String> headers = new ArrayList<>();
        headers.add("turn_number");

        // 1. Initial Player States (Initial condition)
        for (String p : Arrays.asList("pA", "pB")) {
            headers.add(p + "_morale");
            for (int i = 0; i < FIGHTER_COUNT; i++) {
                String f = p + "_f" + i;
                headers.addAll(Arrays.asList(
                        f + "_typeId", f + "_spot", f + "_hp_pct",
                        f + "_en_pct", f + "_isAlive", f + "_isStunned"));
            }
        }

        // 2. Actions (The "Input" - what happened during the turn)
        for (String p : Arrays.asList("pA", "pB")) {
            for (int i = 0; i < MAX_ACTIONS_PER_PLAYER; i++) {
                String a = p + "_act" + i;
                headers.addAll(Arrays.asList(
                        a + "_subjId", a + "_targetId", a + "_abilityId",
                        a + "_cat", a + "_statId", a + "_time",
                        a + "_energy", a + "_dmg", a + "_heal",
                        a + "_blk", a + "_enRes", a + "_statVal",
                        a + "_dur", a + "_stack", a + "_targetType"));
            }
        }

        // 3. Final Player States (The "Resulting State" after actions)
        for (String p : Arrays.asList("pA_final", "pB_final")) {
            headers.add(p + "_morale");
            for (int i = 0; i < FIGHTER_COUNT; i++) {
                String f = p + "_f" + i;
                headers.addAll(Arrays.asList(
                        f + "_typeId", f + "_spot", f + "_hp_pct",
                        f + "_en_pct", f + "_isAlive", f + "_isStunned"));
            }
        }

        // 4. Outcomes (The training labels)
        headers.addAll(Arrays.asList("p1_hp_delta", "p2_hp_delta", "p1_en_delta", "p2_en_delta", "final_win_label"));

        return String.join(",", headers);
    }
}
