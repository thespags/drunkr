package net.spals.drunkr.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;

/**
 * Embeds {@link ObjectId} so its understood by MongoDb instead of simply using the string value.
 *
 * @author spags
 */
public class ObjectIdModule extends Module {

    @Override
    public String getModuleName() {
        return "Drunkr ObjectId Module";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(final SetupContext context) {
        final SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(ObjectIdSerializer.INSTANCE);

        final SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(ObjectId.class, ObjectIdDeserializer.INSTANCE);

        context.addSerializers(serializers);
        context.addDeserializers(deserializers);
    }

    private static class ObjectIdSerializer extends StdSerializer<ObjectId> {

        private static final ObjectIdSerializer INSTANCE = new ObjectIdSerializer();

        private ObjectIdSerializer() {
            super(ObjectId.class);
        }

        @Override
        public void serialize(
            final ObjectId value,
            final JsonGenerator generator,
            final SerializerProvider provider
        ) throws IOException {
            // Ensure ObjectId is written as an embedded object understood by mongo.
            generator.writeStartObject();
            generator.writeStringField("$oid", value.toHexString());
            generator.writeEndObject();
        }
    }

    private static class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

        private static final ObjectIdDeserializer INSTANCE = new ObjectIdDeserializer();

        private ObjectIdDeserializer() {
            super(ObjectId.class);
        }

        @Override
        public ObjectId deserialize(
            final JsonParser parser,
            final DeserializationContext context
        ) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);
            final String objectId;
            if (node.get("$oid") == null) {
                objectId = node.asText();
            } else {
                objectId = node.get("$oid").asText();
            }
            return new ObjectId(objectId);
        }
    }
}
