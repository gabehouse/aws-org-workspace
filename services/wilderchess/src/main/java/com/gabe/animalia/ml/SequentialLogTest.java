// package com.gabe.animalia.ml;

// import com.gabe.animalia.ml.dtos.AbilityDTO;
// import com.gabe.animalia.ml.dtos.LoggableActionDTO;
// import com.gabe.animalia.ml.game.*;
// import com.gabe.animalia.ml.server.GameLogger;
// import java.util.Arrays;
// import java.util.List;
// import java.util.UUID;

// /**
// * A minimal test to verify that the GameLogger correctly creates:
// * 1. A JSON array of turns in one file.
// * 2. A single JSON object for the final result in another file.
// */
// public class SequentialLogTest {

// public static void runSequentialLogVerification() {
// System.out.println("--- Starting Sequential Log Verification Test ---");

// // Generate a unique ID for this test run
// final String GAME_ID = "TEST-" + UUID.randomUUID().toString().substring(0,
// 8);
// final String PLAYER_A = "P-ALPHA";

// GameLogger logger = new GameLogger();

// // --- SIMULATE TURN 1 ---
// System.out.println("Simulating Turn 1...");
// GameTurnOutcome outcome1 = new GameTurnOutcome(-10, 5);

// // Create a fake action for Player A
// AbilityDTO action1 = new AbilityDTO("FOX-01", "Bite", "BEAR-01");
// PlayerActionInput input1 = new PlayerActionInput(PLAYER_A,
// Arrays.asList(action1));

// GameTurnRecord turn1 = new GameTurnRecord(
// GAME_ID, 1, "START-STATE", "MID-STATE", Arrays.asList(input1), outcome1
// );
// logger.logTurnRecord(turn1);

// // --- SIMULATE TURN 2 ---
// System.out.println("Simulating Turn 2...");
// GameTurnOutcome outcome2 = new GameTurnOutcome(-5, 10);
// GameTurnRecord turn2 = new GameTurnRecord(
// GAME_ID, 2, "MID-STATE", "FINAL-STATE", Arrays.asList(input1), outcome2
// );
// logger.logTurnRecord(turn2);

// // --- FINALIZE GAME ---
// System.out.println("Finalizing Game...");
// GameFinalResult result = new GameFinalResult(GAME_ID, PLAYER_A, 2);
// logger.finalizeLog(GAME_ID, result);

// System.out.println("\nSUCCESS: Verification complete.");
// System.out.println("Check the 'gamelogs/' folder in your project root.");
// System.out.println("You should see two files: " + GAME_ID + "$
// turns_slim.json and " + GAME_ID + "$result.json");
// }

// // You can run this file directly if your IDE allows it
// public static void main(String[] args) {
// runSequentialLogVerification();
// }
// }
