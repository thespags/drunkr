package net.spals.drunkr.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.*;

/**
 * A POJO representing a verification code.
 *
 * @author spags
 */
@FreeBuilder
@JsonDeserialize(builder = LinkCode.Builder.class)
public interface LinkCode extends HasTimestamp, HasId {

    ObjectId userId();

    /**
     * Unencrypted code for action authentication.
     */
    String code();

    /**
     * The piece of information being linked...
     */
    String link();

    /**
     * The type of link.
     */
    LinkType type();

    class Builder extends LinkCode_Builder implements HasIdBuilder<LinkCode.Builder> {

        public Builder() {
            id(new ObjectId());
        }
    }
}
