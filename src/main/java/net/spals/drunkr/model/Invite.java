package net.spals.drunkr.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.HasId;
import net.spals.drunkr.model.field.HasIdBuilder;

/**
 * Invite POJO to track signups from invites
 */
@FreeBuilder
@JsonDeserialize(builder = Follower.Builder.class)
public interface Invite extends HasId {

    ObjectId userId();

    String phoneNumber();

    Optional<ObjectId> acceptedUserId();

    class Builder extends Invite_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
        }
    }
}
