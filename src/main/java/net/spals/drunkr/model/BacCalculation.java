package net.spals.drunkr.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.*;

/**
 * Record of BAC calculations for the user
 *
 * @author jbrock
 */
@FreeBuilder
@JsonDeserialize(builder = BacCalculation.Builder.class)
public interface BacCalculation extends HasTimestamp, HasId {

    ObjectId userId();

    double bac();

    class Builder extends BacCalculation_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
        }
    }
}