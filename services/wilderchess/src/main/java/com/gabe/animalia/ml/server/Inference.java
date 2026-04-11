package com.gabe.animalia.ml.server;

import ai.onnxruntime.*;
import java.util.Collections;
import java.util.Map;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

public class Inference {
    private final OrtEnvironment env;
    private final OrtSession session;

    // --- PASTE YOUR UPDATED 351-LENGTH ARRAYS HERE ---
    // Note: I've kept your previous 213 values as a placeholder.
    // You MUST re-copy the 351 values from your latest scaler.pkl/json export.
    private static final float[] MEAN = { 6.32053356f, 5.01327613f, 4.14925342f, 3.31477183f, 0.47742648f, 0.43524873f,
            0.68926911f, 1.00000000f, 5.29551320f, 2.22265855f, 0.62164867f, 0.54978561f, 0.77198860f, 1.00000000f,
            4.50004410f, 4.11667695f, 0.44809782f, 0.42349449f, 0.60721833f, 1.00000000f, 4.49590778f, 4.28790236f,
            0.78243920f, 0.74808998f, 0.88756777f, 1.00000000f, 4.98672387f, 4.13245203f, 3.30011286f, 0.48578155f,
            0.43816719f, 0.69726465f, 1.00000000f, 5.28926927f, 2.18215987f, 0.62735069f, 0.55263922f, 0.77695967f,
            1.00000000f, 4.50185150f, 4.11488735f, 0.43091535f, 0.41333673f, 0.59041561f, 1.00000000f, 4.49161312f,
            4.29497388f, 0.77776253f, 0.74511746f, 0.88389877f, 1.00000000f, 2.02631129f, 60.90131122f, 17.42385415f,
            3.18905922f, 1.20200432f, 0.18538460f, 0.17524849f, 0.12268747f, 0.01366912f, 0.09987537f, 0.03092948f,
            0.19639971f, 0.16161971f, 0.04409674f, 1.87646673f, 2.21287049f, 47.88036368f, 22.30096834f, 2.26019840f,
            1.42745115f, 0.21537717f, 0.25009851f, 0.23878334f, 0.02482525f, 0.07262716f, 0.02837591f, 0.38466310f,
            0.18076158f, 0.08084275f, 1.45183628f, 2.31058663f, 40.30768385f, 22.79775950f, 2.10301453f, 1.41876538f,
            0.22316674f, 0.26132181f, 0.27302771f, 0.02883035f, 0.05659695f, 0.02962497f, 0.44099558f, 0.17309843f,
            0.09111753f, 1.29762772f, 1.89939675f, 28.86066402f, 18.14777732f, 1.68758631f, 1.22002915f, 0.17905704f,
            0.21530962f, 0.22727753f, 0.02528142f, 0.04793359f, 0.02492600f, 0.36189248f, 0.14941801f, 0.08393204f,
            1.07385359f, 0.59192625f, 8.54206724f, 5.11443039f, 0.51706946f, 0.41742292f, 0.05326618f, 0.06441373f,
            0.06845815f, 0.00756992f, 0.01498319f, 0.00736591f, 0.09121407f, 0.05005685f, 0.03205947f, 0.33087715f,
            0.05738748f, 0.74446821f, 0.39210021f, 0.04458373f, 0.05272938f, 0.00437263f, 0.00544128f, 0.00587076f,
            0.00058712f, 0.00121392f, 0.00091882f, 0.00328727f, 0.00642563f, 0.00520833f, 0.02861231f, 0.00080314f,
            0.01358750f, 0.00576545f, 0.00067774f, 0.00066605f, 0.00006736f, 0.00008212f, 0.00008992f, 0.00000956f,
            0.00002028f, 0.00001122f, 0.00005643f, 0.00007689f, 0.00006031f, 0.00045112f, 0.00000106f, 0.00000372f,
            0.00001647f, 0.00000053f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000053f, 0.00000000f, 0.00000000f, 0.00000053f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 5.96868735f, 72.48901640f, 17.37909242f, 3.19898198f, 1.19588791f,
            0.18491942f, 0.17448323f, 0.12132586f, 0.01371901f, 0.09995507f, 0.03070584f, 0.19318853f, 0.16114883f,
            0.04333266f, 1.88089105f, 6.13625014f, 52.68518831f, 22.27433558f, 2.26936052f, 1.41842531f, 0.21514924f,
            0.24923650f, 0.23717746f, 0.02506311f, 0.07309773f, 0.02837064f, 0.37907183f, 0.17971354f, 0.07968121f,
            1.45704434f, 6.20994566f, 43.91989265f, 22.80030494f, 2.10453898f, 1.41176878f, 0.22314976f, 0.26071377f,
            0.27225544f, 0.02917959f, 0.05698924f, 0.02992348f, 0.43617934f, 0.17247253f, 0.09028118f, 1.30068778f,
            5.12679206f, 31.38090593f, 18.18401323f, 1.69412913f, 1.21416062f, 0.17944656f, 0.21537856f, 0.22730556f,
            0.02571662f, 0.04808583f, 0.02498209f, 0.35837028f, 0.14871003f, 0.08332204f, 1.07735920f, 1.60937376f,
            9.34811558f, 5.17928312f, 0.52305701f, 0.41684693f, 0.05378030f, 0.06495609f, 0.06902718f, 0.00768095f,
            0.01504018f, 0.00737933f, 0.09070716f, 0.04980218f, 0.03170319f, 0.33394147f, 0.14818567f, 0.84266044f,
            0.40354901f, 0.04573252f, 0.05277162f, 0.00445572f, 0.00554410f, 0.00595238f, 0.00059477f, 0.00121284f,
            0.00092716f, 0.00298156f, 0.00635347f, 0.00510896f, 0.02915456f, 0.00219529f, 0.01426179f, 0.00616662f,
            0.00073035f, 0.00065250f, 0.00006908f, 0.00008407f, 0.00008880f, 0.00000935f, 0.00002213f, 0.00001219f,
            0.00005492f, 0.00007224f, 0.00005446f, 0.00047370f, 0.00001169f, 0.00011530f, 0.00002976f, 0.00000478f,
            0.00000159f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000053f,
            0.00000000f, 0.00000000f, 0.00000319f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f, 0.00000000f,
            0.00000000f };
    private static final float[] SCALE = { 4.79190104f, 2.86124497f, 2.99628494f, 1.30213097f, 0.41150529f, 0.38620036f,
            0.46279283f, 1.00000000f, 2.98540985f, 1.53048928f, 0.42037733f, 0.38440445f, 0.41955000f, 1.00000000f,
            1.13385644f, 0.94453871f, 0.43407317f, 0.40562032f, 0.48836895f, 1.00000000f, 1.13010604f, 2.82254580f,
            0.35845957f, 0.36229545f, 0.31589749f, 1.00000000f, 2.86124497f, 2.99707465f, 1.30985424f, 0.41174943f,
            0.38446032f, 0.45944168f, 1.00000000f, 2.98602131f, 1.55354679f, 0.41882407f, 0.38288905f, 0.41628517f,
            1.00000000f, 1.13319093f, 0.93062439f, 0.43195750f, 0.40708711f, 0.49175707f, 1.00000000f, 1.13146431f,
            2.80681031f, 0.36176740f, 0.36466530f, 0.32034627f, 1.00000000f, 1.19906254f, 125.25570968f, 14.98827893f,
            2.07560682f, 1.63106372f, 0.06965077f, 0.16331873f, 0.22392969f, 0.05422705f, 0.15653179f, 0.14709947f,
            0.87562928f, 0.31119062f, 0.20531006f, 0.90074994f, 1.13007740f, 149.52471386f, 15.03891969f, 1.63473670f,
            1.95121242f, 0.07582759f, 0.15811229f, 0.26728494f, 0.07756925f, 0.14087927f, 0.15345859f, 1.11926474f,
            0.31995319f, 0.27259347f, 0.97211119f, 1.12489493f, 138.51525670f, 15.15729590f, 1.54036914f, 2.04226120f,
            0.07526049f, 0.15613873f, 0.26984754f, 0.08326260f, 0.13130985f, 0.15881289f, 1.17667380f, 0.31198118f,
            0.28777617f, 0.92591569f, 1.32595769f, 115.85320127f, 15.91971434f, 1.56795032f, 1.96422099f, 0.10390704f,
            0.16920479f, 0.25866383f, 0.07781728f, 0.12289737f, 0.14106887f, 1.09441168f, 0.29858422f, 0.27728586f,
            0.95402200f, 1.14536055f, 63.34241252f, 11.33974853f, 1.16073397f, 1.30697197f, 0.09616605f, 0.13037294f,
            0.16826314f, 0.04411161f, 0.07067809f, 0.07516974f, 0.59911351f, 0.19127200f, 0.17615805f, 0.70940759f,
            0.41576770f, 18.73815321f, 3.34209189f, 0.37032164f, 0.51469469f, 0.03014325f, 0.04049548f, 0.05061531f,
            0.01242872f, 0.02018348f, 0.02345101f, 0.21205105f, 0.07439312f, 0.07198054f, 0.22074759f, 0.04832099f,
            2.68052323f, 0.39265435f, 0.04620372f, 0.05606521f, 0.00379383f, 0.00501129f, 0.00625344f, 0.00161025f,
            0.00255732f, 0.00264320f, 0.02129944f, 0.00804245f, 0.00776563f, 0.02821870f, 0.00162996f, 0.00510258f,
            0.02316606f, 0.00072894f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 0.00103088f, 1.00000000f, 1.00000000f, 0.00072894f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.34222525f, 129.39144211f, 14.95727688f, 2.07567774f, 1.62310676f,
            0.06954207f, 0.16286589f, 0.22282464f, 0.05452489f, 0.15634140f, 0.14640314f, 0.86886411f, 0.31101666f,
            0.20360486f, 0.89849897f, 1.34671387f, 152.04506646f, 14.99280598f, 1.63761755f, 1.94233517f, 0.07577391f,
            0.15770966f, 0.26693510f, 0.07803221f, 0.14109034f, 0.15317274f, 1.11133155f, 0.31941080f, 0.27079903f,
            0.97289308f, 1.41857693f, 141.24328459f, 15.10550221f, 1.53732185f, 2.03545354f, 0.07516998f, 0.15571861f,
            0.26956055f, 0.08384791f, 0.13174087f, 0.15909901f, 1.17093812f, 0.31181213f, 0.28658417f, 0.92780847f,
            2.67186969f, 118.64703162f, 15.87998943f, 1.56795994f, 1.95929222f, 0.10362250f, 0.16875068f, 0.25847187f,
            0.07860876f, 0.12307811f, 0.14116158f, 1.09048857f, 0.29812815f, 0.27636838f, 0.95468507f, 2.80873673f,
            65.39113183f, 11.39510811f, 1.16693667f, 1.30556522f, 0.09648723f, 0.13068286f, 0.16891886f, 0.04450620f,
            0.07085609f, 0.07537366f, 0.59954292f, 0.19071211f, 0.17520874f, 0.71219666f, 0.99021265f, 19.96823803f,
            3.39537841f, 0.37615619f, 0.51431728f, 0.03043528f, 0.04094452f, 0.05102516f, 0.01252015f, 0.02015310f,
            0.02352856f, 0.21585966f, 0.07384310f, 0.07129418f, 0.22280274f, 0.11962485f, 2.52682418f, 0.40950523f,
            0.04871640f, 0.05491389f, 0.00383983f, 0.00508996f, 0.00629864f, 0.00156337f, 0.00272004f, 0.00280284f,
            0.02157144f, 0.00764606f, 0.00737975f, 0.02936712f, 0.00874727f, 0.12562951f, 0.02720614f, 0.00418744f,
            0.00230511f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 0.00103088f,
            1.00000000f, 1.00000000f, 0.00252512f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f, 1.00000000f,
            1.00000000f };

    public Inference(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        // Opset 18 can be memory intensive; OptimizationLevel.BASIC is usually safest
        // for Java
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);

        this.session = env.createSession(modelPath, options);
        System.out.println("ONNX Session Loaded. Expected Inputs: " + session.getInputInfo().keySet());
    }

    public Prediction predict(float[] rawFeatures) throws OrtException {
        // 1. Validation check for the new 351 dimension
        if (rawFeatures.length != MEAN.length) {
            throw new IllegalArgumentException(
                    "Feature mismatch! Expected " + MEAN.length + " but got " + rawFeatures.length);
        }

        // 2. Scaling with Zero-Masking (The "X_raw == 0" Fix)
        float[] scaledFeatures = new float[rawFeatures.length];
        for (int i = 0; i < rawFeatures.length; i++) {
            if (rawFeatures[i] == 0.0f) {
                // If the raw input was 0 (empty slot), keep it 0.
                scaledFeatures[i] = 0.0f;
            } else {
                scaledFeatures[i] = (float) ((rawFeatures[i] - MEAN[i]) / SCALE[i]);
            }
        }

        // Insert after scaledFeatures loop in predict()
        if (Math.random() < 0.01) { // Log roughly 1% of inferences to avoid spam
            float sum = 0;
            float max = -Float.MAX_VALUE;
            for (float f : scaledFeatures) {
                sum += f;
                if (f > max)
                    max = f;
                if (Float.isNaN(f) || Float.isInfinite(f)) {
                    System.err.println("CRITICAL: Feature contains NaN/Inf at index!");
                }
            }
            // System.out.printf("DEBUG: Scaled Mean: %.4f | Scaled Max: %.4f%n", (sum /
            // scaledFeatures.length), max);
        }

        // 3. Prepare Input Tensor
        float[][] inputMatrix = new float[1][scaledFeatures.length];
        inputMatrix[0] = scaledFeatures;

        long startTime = System.nanoTime();
        // Insert before tensor creation
        // Assuming Turn is index 0, P1 Morale is 1, P2 Morale is 2 (adjust indices to
        // your schema)
        // System.out.println(
        // "CANARY: Turn=" + rawFeatures[0] + " | P1_Morale=" + rawFeatures[1] + " |
        // P2_Morale=" + rawFeatures[2]);
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, inputMatrix)) {
            // Fetch the dynamic input name (usually 'input' or 'input.1' in Opset 18)
            String inputName = session.getInputNames().iterator().next();

            try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, tensor))) {
                long durationNs = System.nanoTime() - startTime;
                double durationMs = durationNs / 1_000_000.0;
                // Send this value to a metric buffer or directly to CloudWatch
                try {
                    CloudWatchLogger.logMetric("InferenceLatency", durationMs, "Milliseconds");
                } catch (Throwable t) {
                    // If this prints, your SDK setup is the problem
                    System.err.println(
                            "CRITICAL: CloudWatch SDK Failed: " + t.getClass().getName() + " - " + t.getMessage());
                    t.printStackTrace();
                }
                float winProb = 0.5f;
                if (results.get("win_prob").isPresent()) {
                    float[][] output = (float[][]) results.get("win_prob").get().getValue();
                    winProb = output[0][0];
                } else {
                    System.err.println("ERROR: Could not find 'win_prob' in model output!");
                }

                // THIS IS THE CRITICAL LOG
                // System.out.printf(">>> [INFERENCE] Prob: %.4f | Turn: %.0f | P1_M: %.0f |
                // P2_M: %.0f%n",
                // winProb, rawFeatures[0], rawFeatures[1], rawFeatures[2]);

                // 4. Robust Output Extraction
                // Using .get(index) is often safer than names if the ONNX export renamed them
                // to "output_0"
                OnnxValue winVal = results.get("win_prob").get();
                OnnxValue hpVal = results.get("hp_deltas").get();
                float[][] winProbArray = (float[][]) winVal.getValue();
                float[][] hpDeltasArray = (float[][]) hpVal.getValue();
                return new Prediction(winProbArray[0][0], hpDeltasArray[0]);
            }
        }
    }

    public static class Prediction {
        public final float winProbability;
        public final float p1HpDelta;
        public final float p2HpDelta;

        public Prediction(float winProbability, float[] deltas) {
            // Confidence Interval: Model output is usually Logits or Sigmoid
            // We ensure it stays in 0.0-1.0 range
            this.winProbability = Math.max(0, Math.min(1, winProbability));
            this.p1HpDelta = deltas[0];
            this.p2HpDelta = deltas[1];
        }
    }

}
