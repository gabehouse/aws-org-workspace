package com.gabe.animalia.ml.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for handling JSON serialization and deserialization using Jackson.
 * This class provides the core functionality needed for deep cloning and logging.
 */
public class JsonUtil {

    // ObjectMapper is thread-safe and should be instantiated once and reused.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Optional: Makes the JSON output easy to read for debugging (adds indentation)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Optional: Ensures Java dates/times are serialized cleanly
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Serializes a Java object into a JSON string.
     * Used for logging and the first step of deep cloning.
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            System.err.println("Error serializing object: " + object.getClass().getName() + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Deserializes a JSON string into a specific Java object type.
     */
    public static <T> T fromJson(String jsonString, Class<T> valueType) {
        try {
            return objectMapper.readValue(jsonString, valueType);
        } catch (Exception e) {
            System.err.println("Error deserializing JSON to " + valueType.getName() + " - " + e.getMessage());
            return null;
        }
    }

}
