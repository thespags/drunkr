package net.spals.drunkr.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exposes our custom mapper for tests.
 * <p>
 * The intention is to avoid leaking implementations.
 *
 * @author spags
 * @author tkral
 */
public class ObjectMappers {

    private ObjectMappers() {
    }

    public static ObjectMapper jerseyMapper() {
        return JerseyObjectMapperProvider.JERSEY_MAPPER;
    }

    public static ObjectMapper mongoMapper() {
        return MongoObjectMapperProvider.MONGO_MAPPER;
    }
}
