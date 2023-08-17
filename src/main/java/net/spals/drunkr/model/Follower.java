package net.spals.drunkr.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.HasId;
import net.spals.drunkr.model.field.HasIdBuilder;

/**
 * Join pojo between two {@link Person}.
 *
 * @author spags
 */
@FreeBuilder
@JsonDeserialize(builder = Follower.Builder.class)
public interface Follower extends HasId {

    ObjectId followerId();

    ObjectId userId();

    class Builder extends Follower_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
        }
    }
}
