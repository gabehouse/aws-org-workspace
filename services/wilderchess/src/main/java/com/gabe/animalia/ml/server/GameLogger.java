package com.gabe.animalia.ml.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gabe.animalia.ml.game.GameFinalResult;
import com.gabe.animalia.ml.game.GameSessionLog;
import com.gabe.animalia.ml.game.GameTurnRecord;
import com.gabe.animalia.ml.game.StateSnapshotLogEntry;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class GameLogger {

    private final String logDirectory = "gamelogs/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameLogger() {
        File dir = new File(logDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Make JSON pretty for easier debugging
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Saves the entire game session to a single file.
     * The file name will be [gameId].json
     */
    public void saveGameSession(GameSessionLog session, String filenameIdentifier) {
        if (session == null || filenameIdentifier == null) return;

        // Use the provided identifier for the filename
        String filename = logDirectory + filenameIdentifier + ".json";
        try {
            objectMapper.writeValue(new File(filename), session);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to save game session " + filenameIdentifier + ": " + e.getMessage());
        }
    }
}
