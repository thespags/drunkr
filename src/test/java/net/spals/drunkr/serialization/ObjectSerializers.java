package net.spals.drunkr.serialization;

/**
 * Exposes our custom serializers for tests.
 * <p>
 * The intention is to avoid leaking implementations.
 *
 * @author spags
 * @author tkral
 */
public class ObjectSerializers {

    private ObjectSerializers() {
    }

    public static ObjectSerializer createObjectSerializer() {
        return new ObjectSerializer(ObjectMappers.jerseyMapper());
    }
}
