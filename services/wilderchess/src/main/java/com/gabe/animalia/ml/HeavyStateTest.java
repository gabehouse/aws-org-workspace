// package com.gabe.animalia.ml;

// import com.gabe.animalia.enums.TargetTypeEnum;
// import com.gabe.animalia.ml.dtos.FighterStateDTO;
// import com.gabe.animalia.ml.dtos.LoggableActionDTO;
// import com.gabe.animalia.ml.dtos.PlayerStateDTO;
// import com.gabe.animalia.ml.game.GameFeaturizer;
// import com.gabe.animalia.ml.game.PlayerActionInput;
// import com.gabe.animalia.ml.mapper.FighterType;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;

// public class HeavyStateTest {

// public static void runHeavyStateVerification() {
// System.out.println("--- Starting Schema & Normalization Verification ---");

// GameFeaturizer featurizer = new GameFeaturizer();

// // 1. Build Dummy Fighters
// // Testing: currentHp/maxHp ratio (should be 0.8000) and currentEnergy/100
// // (should be 0.2000)
// FighterStateDTO f1 = new FighterStateDTO(1, FighterType.FOX, 80, 100, 20,
// "pos_0", 0, true, false);

// // 2. Build Dummy Players
// // Using P-ALPHA and P-BETA strings for ID, but we'll use 0/1 for the action
// // mapping
// PlayerStateDTO pA = new PlayerStateDTO("P-ALPHA", 5, Arrays.asList(f1), new
// ArrayList<>());
// PlayerStateDTO pB = new PlayerStateDTO("P-BETA", 3, new ArrayList<>(), new
// ArrayList<>());

// // 3. Build a Dummy Action for Player 0 (P-ALPHA)
// LoggableActionDTO action0 = new LoggableActionDTO();
// action0.setSubjectId(112); // Stable Square ID
// action0.setTargetId(900); // Bench ID
// action0.setTargetType(TargetTypeEnum.SQUARE);
// action0.setDamage(50); // Expect 0.5000
// action0.setDuration(999); // Expect 1.0000 (Permanent)
// action0.setStatusValue(8); // Expect 0.8000
// action0.setEnergyCost(40); // Expect 0.4000
// action0.setStackable(true); // Expect 1.0

// // 4. Build a Dummy Action for Player 1 (P-BETA)
// LoggableActionDTO action1 = new LoggableActionDTO();
// action1.setSubjectId(115);
// action1.setTargetId(112); // Targeting the Fox
// action1.setTargetType(TargetTypeEnum.FIGHTER);
// action1.setDamage(10); // Expect 0.1000
// action1.setDuration(2); // Expect 0.2000
// action1.setStackable(false); // Expect 0.0

// // Wrap into Joint Actions
// PlayerActionInput inputA = new PlayerActionInput(0, Arrays.asList(action0));
// PlayerActionInput inputB = new PlayerActionInput(1, Arrays.asList(action1));
// List<PlayerActionInput> jointActions = Arrays.asList(inputA, inputB);

// // 5. Generate Features
// float[] features = featurizer.featurizeForInference(1, pA, pB, jointActions);

// // 6. Validation Prints
// System.out.println("\n--- STATE VERIFICATION ---");
// System.out.println("P1 Morale: " + features[1]);
// System.out.println("P1 Fighter 0 HP Pct: " + features[3]); // 1 (turn) + 1
// (morale) + 1 (type) + 1 (hp)
// System.out.println("P1 Fighter 0 Energy: " + features[4]);

// // Calculation: 1 (turn) + 36 (pA state) + 36 (pB state) = Index 73
// int pA_ActionStart = 73;
// int pB_ActionStart = 73 + 140; // pA has 10 action slots * 14 fields

// System.out.println("\n--- PLAYER A ACTION 0 (PERMANENT BUFF) ---");
// System.out.println("SubjectId: " + (int) features[pA_ActionStart]);
// System.out.println("TargetId: " + (int) features[pA_ActionStart + 2]);
// System.out.println("Damage (0.5000): " + features[pA_ActionStart + 7]);
// System.out.println("Duration (1.0000): " + features[pA_ActionStart + 12]);

// System.out.println("\n--- PLAYER B ACTION 0 (SHORT ATTACK) ---");
// System.out.println("Damage (0.1000): " + features[pB_ActionStart + 7]);
// System.out.println("Duration (0.2000): " + features[pB_ActionStart + 12]);

// System.out.println("\n--- FINAL TOTALS ---");
// System.out.println("Total Feature Count: " + features.length);

// int expectedSize = 1 + (2 * 36) + (2 * 140);
// if (features.length == expectedSize) {
// System.out.println("✅ SUCCESS: Schema count is exactly " + expectedSize);
// } else {
// System.err.println("❌ ERROR: Mismatch! Got " + features.length + " but
// expected " + expectedSize);
// }
// }

// public static void main(String[] args) {
// runHeavyStateVerification();
// }
// }
