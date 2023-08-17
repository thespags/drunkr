package net.spals.drunkr.common;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import com.google.common.truth.Correspondence;

import com.mongodb.DBObject;

/**
 * Allows us to compare a list of {@link DBObject} using google's Truth where we only care about the
 * value a field in the index.
 *
 * @author spags
 */
public class DBObjectCorrespondence extends Correspondence<DBObject, String> {

    private static final DBObjectCorrespondence INSTANCE = new DBObjectCorrespondence();

    private DBObjectCorrespondence() {
    }

    public static DBObjectCorrespondence get() {
        return INSTANCE;
    }

    @Override
    public boolean compare(@Nullable final DBObject actual, @Nullable final String expected) {
        //noinspection unchecked
        final Map<String, Object> index = (Map<String, Object>) Objects.requireNonNull(actual).get("key");
        return index.containsKey(expected);
    }

    @Override
    public String toString() {
        return "a mongo db index";
    }
}
