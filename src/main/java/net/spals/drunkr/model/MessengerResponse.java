package net.spals.drunkr.model;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.service.messenger.MessengerClient;

/**
 * Response to user when interacting with Messenger and sent using {@link MessengerClient}
 * Facebook Messenger API is looking for messaging_type and tag -- drunkr does not consume these.
 *
 * @author jbrock
 */
@FreeBuilder
@JsonDeserialize(builder = MessengerResponse.Builder.class)
public interface MessengerResponse {

    Map<String, String> recipient();

    Map<String, String> message();

    /**
     * Required attribute for Messenger API to identify type of message being sent by bots.
     *
     * @return {@link MessageType}
     */
    @JsonProperty("messaging_type")
    MessageType messagingType();

    /**
     * Required attribute when {@link #messagingType()} is MESSAGE_TAG and sending to person > 24 hours since
     * they last interacted with our bot via Messenger.
     *
     * @return {@link Tag}
     */
    Optional<Tag> tag();

    enum MessageType {
        RESPONSE,
        UPDATE,
        MESSAGE_TAG
    }

    enum Tag {
        GAME_EVENT,
        PAIRING_UPDATE
    }

    class Builder extends MessengerResponse_Builder {

    }
}
