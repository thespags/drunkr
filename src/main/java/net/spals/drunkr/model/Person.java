package net.spals.drunkr.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.HasId;
import net.spals.drunkr.model.field.HasIdBuilder;

/**
 * Pojo for some drunkr person.
 *
 * @author spags
 */
@FreeBuilder
@JsonDeserialize(builder = Person.Builder.class)
public interface Person extends HasId {

    String userName();

    double weight();

    Gender gender();

    Optional<String> phoneNumber();

    Optional<String> messengerId();

    class Builder extends Person_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
        }
    }
}
