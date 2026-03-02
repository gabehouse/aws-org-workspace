// package com.gabe.animalia.ml;

// import com.gabe.animalia.ml.util.JsonUtil;

// public class MinimalOutputTest {

//     public static void runFileWriteVerification() {
//         System.out.println("--- Starting Minimal File Write Verification ---");

//         // 1. Define the raw, minimal content (simulating the final Game Result JSON)
//         String minimalJsonOutput =
//             "{\n" +
//             "  \"gameId\" : \"GAME-1234\",\n" +
//             "  \"winningPlayerId\" : \"P-ALPHA\",\n" +
//             "  \"totalTurns\" : 10\n" +
//             "}";

//         final String FILENAME = "final_result_output_test.json";

//         // 2. Use the utility to write the raw string to the file
//         JsonUtil.writeStringToFile(FILENAME, minimalJsonOutput);

//         System.out.println("--- Minimal File Write Test Complete ---");
//     }

//     // Placeholder main to allow easy local execution in an IDE
//     public static void main(String[] args) {
//         runFileWriteVerification();
//     }
// }
