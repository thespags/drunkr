package net.spals.drunkr.common;

import static com.google.common.truth.Truth.assertAbout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

import org.bson.types.ObjectId;

import net.spals.drunkr.model.Notification;
import net.spals.drunkr.model.Source;

/**
 * A Google Truth subject to assert against {@link Notification} pojo's.
 *
 * @author spags
 */
public class NotificationSubject extends Subject<NotificationSubject, Notification> {

    private static final Subject.Factory<NotificationSubject, Notification> NOTIFICATION_SUBJECT_FACTORY = NotificationSubject::new;

    private NotificationSubject(final FailureMetadata metadata, @Nullable final Notification actual) {
        super(metadata, actual);
    }

    // User-defined entry point
    public static NotificationSubject assertThat(@Nullable final Notification notification) {
        return assertAbout(NOTIFICATION_SUBJECT_FACTORY).that(notification);
    }

    // Static method for getting the subject factory (for use with assertAbout())
    public static Subject.Factory<NotificationSubject, Notification> notifications() {
        return NOTIFICATION_SUBJECT_FACTORY;
    }

    public NotificationSubject hasSourceUserId(@Nonnull final ObjectId sourceUserId) {
        final boolean matched = actual().sourceUserId()
            .map(x -> Objects.equals(x, Objects.requireNonNull(sourceUserId)))
            .orElse(false);
        if (!matched) {
            fail("has a sourceUserId", sourceUserId);
        }
        return this;
    }

    public NotificationSubject hasEmptySourceUserId() {
        actual().sourceUserId().ifPresent(x -> fail("has a sourceUserId", x));
        return this;
    }

    public NotificationSubject hasSource(@Nonnull final Source source) {
        final boolean matched = actual().source()
            .map(x -> x == source)
            .orElse(false);
        if (!matched) {
            fail("has a source", source);
        }
        return this;
    }

    public NotificationSubject hasUserId(@Nonnull final ObjectId userId) {
        if (!Objects.equals(actual().userId(), userId)) {
            fail("has a userId", userId);
        }
        return this;
    }

    public NotificationSubject hasMessage(@Nonnull final String message) {
        if (!Objects.equals(actual().message(), message)) {
            fail("has a message", message);
        }
        return this;
    }

    public NotificationSubject isPushed() {
        if (!actual().pushed()) {
            fail("should be pushed");
        }
        return this;
    }

    public NotificationSubject isNotPushed() {
        if (actual().pushed()) {
            fail("should NOT be pushed");
        }
        return this;
    }
}
