package net.spals.drunkr.model.field;

import java.time.ZonedDateTime;

/**
 * Represents an object that has a temporal field.
 *
 * @author spags
 */
public interface HasTimestamp {

    ZonedDateTime timestamp();
}
