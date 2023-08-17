package net.spals.drunkr.common;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.google.common.truth.Correspondence;

import net.spals.drunkr.model.field.HasTimestamp;

/**
 * Allows us to compare a list of {@link HasTimestamp} using google's Truth where we only care about the
 * value of {@link HasTimestamp#timestamp()}.
 *
 * @author spags
 */
public class HasTimestampCorrespondence extends Correspondence<HasTimestamp, ZonedDateTime> {

    private static final HasTimestampCorrespondence INSTANCE = new HasTimestampCorrespondence();

    private HasTimestampCorrespondence() {
    }

    public static HasTimestampCorrespondence get() {
        return INSTANCE;
    }

    @Override
    public boolean compare(
        @Nullable final HasTimestamp actual,
        @Nullable final ZonedDateTime expected
    ) {
        return Objects.equals(Objects.requireNonNull(actual).timestamp(), expected);
    }

    @Override
    public String toString() {
        return "a timestamp object with the expected time";
    }
}
