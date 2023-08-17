package net.spals.drunkr.common;

import static com.google.common.truth.Truth.assertAbout;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

/**
 * A Google Truth subject to assert against {@link Response} pojo's.
 *
 * @author spags
 */
public class ResponseSubject extends Subject<ResponseSubject, Response> {

    private static final Subject.Factory<ResponseSubject, Response> RESPONSE_SUBJECT_FACTORY = ResponseSubject::new;

    private ResponseSubject(final FailureMetadata metadata, @Nullable final Response actual) {
        super(metadata, actual);
    }

    // User-defined entry point
    public static ResponseSubject assertThat(@Nullable final Response response) {
        return assertAbout(RESPONSE_SUBJECT_FACTORY).that(response);
    }

    // Static method for getting the subject factory (for use with assertAbout())
    public static Subject.Factory<ResponseSubject, Response> responses() {
        return RESPONSE_SUBJECT_FACTORY;
    }

    public ResponseSubject hasStatus(final Response.Status status) {
        if (actual().getStatus() != status.getStatusCode()) {
            fail("has a status", status.getStatusCode());
        }
        return this;
    }

    public ResponseSubject hasEntity(final Object entity) {
        if (!Objects.equals(actual().getEntity(), entity)) {
            fail("has an entity", entity);
        }
        return this;
    }

    /**
     * Do the given list of entities appear as a {@link List} entity?
     * <p>
     * This is a convenience function to avoid explicitly constructing the list ourselves.
     */
    public ResponseSubject hasListOfEntities(final Object... entities) {
        final List<Object> entity = ImmutableList.copyOf(entities);
        if (!Objects.equals(actual().getEntity(), entity)) {
            fail("has an entity", entity);
        }
        return this;
    }

    public ResponseSubject hasErrorMessage(final String message) {
        if (!Objects.equals(actual().getEntity(), ImmutableMap.of("message", message))) {
            fail("has an error message", message);
        }
        return this;
    }
}
