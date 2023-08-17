package net.spals.drunkr.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Gives a status report of a user, their BAC and whether or not they are drinking.
 *
 * @author spags
 */
@FreeBuilder
@JsonDeserialize(builder = Person.Builder.class)
public interface BacStatus {

    Person user();

    double bac();

    boolean isDrinking();

    class Builder extends BacStatus_Builder {

    }
}
