package net.spals.drunkr.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.model.field.*;

/**
 * A notification to be sent to user for invites, jobs, followings, etc.
 *
 * @author spags
 */
@FreeBuilder
@JsonDeserialize(builder = Notification.Builder.class)
public interface Notification extends HasTimestamp, HasId {

    /**
     * The user the notification is destined to.
     *
     * @return the user id
     */
    ObjectId userId();

    /**
     * The user the notification is from.
     *
     * @return the user id originated the notification, empty if from the system
     */
    Optional<ObjectId> sourceUserId();

    /**
     * If a source is specified will give priority to that notification stream.
     */
    Optional<Source> source();

    /**
     * The text of the message.
     *
     * @return the text
     */
    String message();

    /**
     * Whether or not the user has read the message or not.
     *
     * @return true if the message been read otherwise false
     */
    boolean read();

    /**
     * Whether or not a job processed this message and sent it to the user.
     *
     * @return true if the message has been sent otherwise false
     */
    boolean pushed();

    class Builder extends Notification_Builder implements HasIdBuilder<Builder> {

        public Builder() {
            id(new ObjectId());
            read(false);
            pushed(false);
        }
    }
}
