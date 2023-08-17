package net.spals.drunkr.db;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.netflix.governator.annotations.Configuration;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindProvider;

/**
 * Helper to create a {@link DB} through a mongo uri provided by the a configuration.
 *
 * @author spags
 */
@AutoBindProvider
class MongoDbProvider implements Provider<MongoDatabase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbProvider.class);
    private final ObjectMapper mapper;
    @NotNull
    @Configuration("mongo.uri")
    private String mongoUri;
    private MongoDatabase database;

    @Inject
    MongoDbProvider(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    private void createDb() {
        createDb(mongoUri);
    }

    @SuppressWarnings("deprecation")
    @VisibleForTesting
    void createDb(final String mongoUri) {
        LOGGER.info("MongoUri=" + mongoUri);
        final CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClient.getDefaultCodecRegistry(),
            fromProviders(new JacksonCodecProvider(mapper))
        );
        final MongoClientURI mongoClientUri = new MongoClientURI(mongoUri);
        final MongoClient mongoClient = new MongoClient(mongoClientUri);
        database = mongoClient.getDatabase(mongoClientUri.getDatabase())
            .withCodecRegistry(pojoCodecRegistry);
    }

    @Override
    public MongoDatabase get() {
        return database;
    }
}
