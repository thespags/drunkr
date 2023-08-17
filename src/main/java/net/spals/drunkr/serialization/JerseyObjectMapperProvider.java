package net.spals.drunkr.serialization;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import java.time.ZoneOffset;
import java.util.TimeZone;

import com.google.common.annotations.VisibleForTesting;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * Provider for Jersey to use a custom constructed {@link ObjectMapper}.
 *
 * @author spags
 * @author tkral
 */
@AutoBindSingleton
@Provider
class JerseyObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @VisibleForTesting
    static final ObjectMapper JERSEY_MAPPER = buildMapper();

    JerseyObjectMapperProvider() {
    }

    private static ObjectMapper buildMapper() {
        final Module objectIdModule = new SimpleModule()
            .addSerializer(ObjectId.class, ToStringSerializer.instance);

        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .registerModule(new Jdk8Module().configureAbsentsAsNulls(true))
            .registerModule(objectIdModule);
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return JERSEY_MAPPER;
    }
}
