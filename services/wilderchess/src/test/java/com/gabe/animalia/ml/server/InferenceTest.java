// package com.gabe.animalia.ml.server;

// import ai.onnxruntime.OrtException;

// import com.gabe.animalia.enums.ActionCategoryEnum;
// import com.gabe.animalia.ml.dtos.FighterStateDTO;
// import com.gabe.animalia.ml.dtos.LoggableActionDTO;
// import com.gabe.animalia.ml.dtos.PlayerStateDTO;
// import com.gabe.animalia.ml.game.GameFeaturizer;
// import com.gabe.animalia.ml.game.PlayerActionInput;
// import com.gabe.animalia.ml.mapper.FighterType; // Assuming this is the
// package for FighterType

// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;

// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// class InferenceTest {

// private static Inference inference;
// private static GameFeaturizer featurizer;

// @BeforeAll
// static void setup() throws OrtException {
// // Load model from test resources
// String modelPath = "src/main/resources/wilderchess.onnx";
// inference = new Inference(modelPath);
// featurizer = new GameFeaturizer();
// }

// @Test
// @DisplayName("Model should predict low win probability for a losing board
// state")
// void testLosingPosition() throws OrtException {
// // Reverse the roles from your dominant test
// PlayerStateDTO pA = new PlayerStateDTO("PlayerA", 5, createTeam(1, 0, false),
// new ArrayList<>());
// PlayerStateDTO pB = new PlayerStateDTO("PlayerB", 100, createTeam(500, 100,
// true), new ArrayList<>());

// float[] features = featurizer.featurizeForInference(20, pA, pB,
// Collections.emptyList());
// Inference.Prediction prediction = inference.predict(features);

// System.out.println("Losing Win Prob: " + prediction.winProbability);
// assertTrue(prediction.winProbability < 0.25f, "Model should predict a loss
// for Player A");
// }

// @Test
// @DisplayName("Model should predict near 50% for an even board state")
// void testEvenPosition() throws OrtException {
// PlayerStateDTO pA = new PlayerStateDTO("PlayerA", 50, createTeam(250, 50,
// true), new ArrayList<>());
// PlayerStateDTO pB = new PlayerStateDTO("PlayerB", 50, createTeam(250, 50,
// true), new ArrayList<>());

// float[] features = featurizer.featurizeForInference(20, pA, pB,
// Collections.emptyList());
// Inference.Prediction prediction = inference.predict(features);

// System.out.println("Even Win Prob: " + prediction.winProbability);
// // Even models aren't perfect, so a range of 0.3 to 0.7 is a good sanity
// check
// assertTrue(prediction.winProbability > 0.3f && prediction.winProbability <
// 0.7f,
// "Even match should be near 0.5, got: " + prediction.winProbability);
// }

// @Test
// @DisplayName("Model should handle a state where all fighters are dead")
// void testTotalExtermination() throws OrtException {
// PlayerStateDTO pA = new PlayerStateDTO("PlayerA", 0, createTeam(0, 0, false),
// new ArrayList<>());
// PlayerStateDTO pB = new PlayerStateDTO("PlayerB", 0, createTeam(0, 0, false),
// new ArrayList<>());

// float[] features = featurizer.featurizeForInference(0, pA, pB,
// Collections.emptyList());
// Inference.Prediction prediction = inference.predict(features);

// assertNotNull(prediction);
// assertFalse(Float.isNaN(prediction.winProbability), "Prediction should not be
// NaN");
// }

// @Test
// @DisplayName("Model should predict high win probability for a dominant board
// state")
// void testDominantPosition() throws OrtException {
// // 1. Setup Player A (Winning) - (ID, Morale, Fighters, Buffs)
// PlayerStateDTO pA = new PlayerStateDTO("PlayerA", 100, createTeam(500, 100,
// true), new ArrayList<>());

// // 2. Setup Player B (Losing)
// PlayerStateDTO pB = new PlayerStateDTO("PlayerB", 5, createTeam(1, 0, false),
// new ArrayList<>());

// // 3. Mock a simple attack action - (SubjectId, SubjType, TargetId,
// TargetType,
// // AbilityId, Time, Desc, Icon, Energy, Scale, X, Y)
// // I used placeholders based on your 12-argument requirement
// LoggableActionDTO attackAction = new LoggableActionDTO(0, 1, "0",
// ActionCategoryEnum.ATTACK, "10", 50, "Attack",
// "", 10, 1.0, 0, 0);

// List<PlayerActionInput> actions = List.of(
// new PlayerActionInput(0, List.of(attackAction)),
// new PlayerActionInput(1, Collections.emptyList()));

// // 4. Featurize
// float[] features = featurizer.featurizeForInference(20, pA, pB, actions);

// // 5. Assert Shape
// assertEquals(213, features.length, "Input features must match model expected
// input_dim");

// // 6. Predict and Assert Logic
// Inference.Prediction prediction = inference.predict(features);

// System.out.println("Predicted Win Probability: " + (prediction.winProbability
// * 100) + "%");
// // Inside testDominantPosition
// assertTrue(prediction.p2HpDelta <= 0, "Player B should be losing HP in a
// losing state");
// assertTrue(prediction.winProbability > 0.75f,
// "Model should be confident in victory, but returned: " +
// prediction.winProbability);
// }

// // Helper to fill the 7 fighter slots to match your 9-argument constructor
// // (Id, Type, MaxHp, CurrentHp, CurrentEnergy, Name, Level, Alive, Stunned)
// private List<FighterStateDTO> createTeam(int hp, int energy, boolean alive) {
// List<FighterStateDTO> fighters = new ArrayList<>();
// for (int i = 0; i < 7; i++) {
// if (i == 0) {
// fighters.add(new FighterStateDTO(1, FighterType.LION, 500, hp, energy,
// "Unit1", 1, alive, false));
// } else {
// // Empty slots
// fighters.add(new FighterStateDTO(0, FighterType.NONE, 0, 0, 0, "None", 0,
// false, false));
// }
// }
// return fighters;
// }
// }
