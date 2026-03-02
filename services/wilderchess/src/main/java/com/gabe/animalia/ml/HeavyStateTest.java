// package com.gabe.animalia.ml;

// import com.gabe.animalia.ml.dtos.FighterStateDTO;
// import com.gabe.animalia.ml.dtos.PlayerStateDTO;
// import com.gabe.animalia.ml.game.GameFinalResult;
// import com.gabe.animalia.ml.game.StateSnapshotLogEntry;
// import com.gabe.animalia.ml.game.TwoPlayerGameState;
// import com.gabe.animalia.ml.server.GameLogger;
// import java.util.Arrays;

// public class HeavyStateTest {

//     public static void runHeavyStateVerification() {
//         System.out.println("--- Starting Heavy State Verification Test ---");

//         GameLogger logger = new GameLogger();
//         String gameId = "HEAVY-TEST";
//         String s0_id = gameId + "-S0";

//         // 1. Build a dummy Fighter
//         FighterStateDTO f1 = new FighterStateDTO("FOX-1", "Fox", 100, 100, 20, "A1", 0, false);

//         // 2. Build dummy Players
//         PlayerStateDTO pA = new PlayerStateDTO("P-ALPHA", 5, 10, Arrays.asList(f1));
//         PlayerStateDTO pB = new PlayerStateDTO("P-BETA", 5, 10, Arrays.asList());

//         // 3. Build the Game State
//         TwoPlayerGameState state = new TwoPlayerGameState(1, pA, pB);

//         // 4. Log it using our existing logger
//         StateSnapshotLogEntry entry = new StateSnapshotLogEntry(s0_id, state);
//         logger.logStateSnapshot(gameId, entry);

//         // 6. Create a real result to verify result file writing
//         GameFinalResult result = new GameFinalResult(gameId, "P-ALPHA", 1);

//         // 7. Finalize to close streams and write result
//         logger.finalizeLog(gameId, result);

//         System.out.println("SUCCESS: Check 'gamelogs/HEAVY-TEST_states_heavy.json' and 'gamelogs/HEAVY-TEST_result.json'");
//     }

//     public static void main(String[] args) {
//         runHeavyStateVerification();
//     }
// }
