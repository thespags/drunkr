package net.spals.drunkr.db;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonWriterSettings;

/**
 * Codec for Mongo to understand Jackson serialization.
 *
 * @author spags
 */
public class JacksonCodec<T> implements Codec<T> {

    //https://stackoverflow.com/questions/35209839/converting-document-objects-in-mongodb-3-to-pojos
    private static final JsonWriterSettings SETTINGS = JsonWriterSettings.builder()
        .int64Converter((value, writer) -> writer.writeNumber(value.toString()))
        .build();
    private final ObjectMapper objectMapper;
    private final Codec<RawBsonDocument> rawBsonDocumentCodec;
    private final Class<T> type;

    JacksonCodec(
        final ObjectMapper objectMapper,
        final CodecRegistry codecRegistry,
        final Class<T> type
    ) {
        this.objectMapper = objectMapper;
        rawBsonDocumentCodec = codecRegistry.get(RawBsonDocument.class);
        this.type = type;
    }

    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        try {
            final BsonDocument document = rawBsonDocumentCodec.decode(reader, decoderContext);
            final String json = document.toJson(SETTINGS);
            return objectMapper.readValue(json, type);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void encode(final BsonWriter writer, final Object value, final EncoderContext encoderContext) {
        try {
            final String data = objectMapper.writeValueAsString(value);
            rawBsonDocumentCodec.encode(writer, RawBsonDocument.parse(data), encoderContext);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Class<T> getEncoderClass() {
        return type;
    }
}
