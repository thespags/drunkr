package net.spals.drunkr.model.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Similar to {@link HasId} for POJOs but for {@link FreeBuilder} builders.
 *
 * @author tkral
 */
public interface HasIdBuilder<B> {

    @JsonProperty("_id")
    B id(ObjectId id);
}
