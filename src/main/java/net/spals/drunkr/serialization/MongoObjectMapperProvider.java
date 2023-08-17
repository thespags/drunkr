package net.spals.drunkr.serialization;

import java.time.ZoneOffset;
import java.util.TimeZone;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.spals.appbuilder.annotations.service.AutoBindProvider;

/**
 * Our own {@link ObjectMapper} to handle serialization and deserialization to play friendly with mongo and anything else.
 *
 * @author spags
 * @author tkral
 */
@AutoBindProvider
public class MongoObjectMapperProvider implements Provider<ObjectMapper> {

    @VisibleForTesting
    public static final ObjectMapper MONGO_MAPPER = buildMapper();

    @Inject
    MongoObjectMapperProvider() {
    }

    private static ObjectMapper buildMapper() {

        return new ObjectMapper()
            .registerModule(new ObjectIdModule())
            .registerModule(new JavaTimeModule())
            .setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
    }

    @Override
    public ObjectMapper get() {
        return MONGO_MAPPER;
    }
}
