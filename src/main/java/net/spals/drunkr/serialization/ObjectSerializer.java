package net.spals.drunkr.serialization;

import java.util.Map;

import com.google.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helpers for (de)serialization our POJOs using our custom {@link ObjectMapper}.
 *
 * @author spags
 */
// This should have an interface to bind against but I've been lazy because I'm not sure how long this class will exist.
public class ObjectSerializer {

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };

    private final ObjectMapper mapper;

    @Inject
    ObjectSerializer(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T patch(final T object, final Map<String, Object> payload, final Class<T> clazz) {
        final Map<String, Object> objectMap = mapper.convertValue(object, MAP_TYPE_REFERENCE);
        objectMap.putAll(payload);
        return mapper.convertValue(objectMap, clazz);
    }
}