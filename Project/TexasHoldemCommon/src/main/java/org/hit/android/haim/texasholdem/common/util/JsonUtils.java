package org.hit.android.haim.texasholdem.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Haim Adrian
 * @since 03-Jul-21
 */
public class JsonUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = createObjectMapper();
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    public static <V> String writeValueAsString(V value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    public static <V> V readValueFromString(String value, Class<V> cls) throws JsonProcessingException {
        return objectMapper.readValue(value, cls);
    }
}

