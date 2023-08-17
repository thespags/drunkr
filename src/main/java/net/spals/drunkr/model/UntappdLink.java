package net.spals.drunkr.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.HasId;
import net.spals.drunkr.model.field.HasIdBuilder;

/**
 * Associate users with Untappd Accounts
 *
 * @author jbrock
 */
@FreeBuilder
@JsonDeserialize(builder = UntappdLink.Builder.class)
public interface UntappdLink extends HasId {

    ObjectId userId();

    String untappdName();

    /**
     * Encrypted form of the user's access token.
     *
     * @return the encrypted access token for this user
     */
    Optional<String> accessToken();

    class Builder extends UntappdLink_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
        }
    }
}
