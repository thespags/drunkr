package net.spals.drunkr.model.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;

/**
 * Represents an object that has a unique identifier using {@link ObjectId}.
 * Mongo expects the field to be "_id" but that feels awkward for java so we convert it from "id" <-> "_id".
 *
 * @author spags
 */
public interface HasId {

    @JsonProperty("_id")
    ObjectId id();
}