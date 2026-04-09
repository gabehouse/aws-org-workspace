package com.gabe.animalia.ml.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.gabe.animalia.ml.game.GameFeaturizer;

import com.gabe.animalia.ml.game.GameSessionLog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.FileWriter;

import java.util.List;

public class GameLogger {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Define the two distinct base directories
    private final String jsonBaseDir;
    private final String csvBaseDir;

    public GameLogger() {
        // 1. Determine JSON path (Default: gamelogs/)
        String envJsonPath = System.getenv("GAME_LOG_DIR");
        this.jsonBaseDir = normalizePath(envJsonPath != null ? envJsonPath : "gamelogs/");

        // 2. Determine CSV path (Default: ml_data/)
        String envCsvPath = System.getenv("ML_DATA_DIR");
        this.csvBaseDir = normalizePath(envCsvPath != null ? envCsvPath : "ml_data/");

        // Ensure both base directories exist
        ensureDirectoryExists(jsonBaseDir);
        ensureDirectoryExists(csvBaseDir);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private String normalizePath(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void saveGameSession(GameSessionLog session, String statusAndTimestamp) {
        if (session == null || statusAndTimestamp == null)
            return;

        // Flat filename: gamelogs/uuid-123_SUCCESS-2026-04-02.json
        String flatName = session.getGameId() + "_" + statusAndTimestamp + ".json";
        File file = new File(jsonBaseDir + flatName);

        try {
            objectMapper.writeValue(file, session);
            System.out.println("JSON Log Saved: " + file.getName());
        } catch (IOException e) {
            System.err.println("JSON Save Failed: " + e.getMessage());
        }
    }

    public void saveToCsvFile(String gameId, String statusAndTimestamp, List<String> rows) {
        if (rows.isEmpty() || gameId == null)
            return;

        // Flat filename: ml_data/uuid-123_SUCCESS-2026-04-02.csv
        String flatName = gameId + "_" + statusAndTimestamp + ".csv";
        File file = new File(csvBaseDir + flatName);

        try (PrintWriter out = new PrintWriter(new FileWriter(file, false))) {
            out.println(new GameFeaturizer().getCsvHeader());
            for (String row : rows) {
                out.println(row);
            }
            System.out.println("CSV Data Saved: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
