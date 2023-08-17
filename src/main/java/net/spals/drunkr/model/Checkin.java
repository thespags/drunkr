package net.spals.drunkr.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.*;

/**
 * Pojo for an untappd checkin.
 *
 * @author spags
 */
@FreeBuilder
@JsonDeserialize(builder = Checkin.Builder.class)
public interface Checkin extends HasTimestamp, HasId {

    String name();

    ObjectId userId();

    Optional<String> producer();

    /**
     * Currently we are grabbing this as rating * 100, so a rating for 2.25 is 225, 5.0 is 500.
     */
    Optional<Integer> rating();

    double abv();

    double size();

    Style style();

    class Builder extends Checkin_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
            style(Style.NONE);
        }
    }
}