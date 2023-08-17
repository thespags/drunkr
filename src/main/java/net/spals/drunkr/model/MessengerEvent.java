package net.spals.drunkr.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Messenger POST event received from Webhook request sent by Messenger API.
 * This represents an inbound message from Messenger to drunkr.
 *
 * @author jbrock
 */
@FreeBuilder
@JsonDeserialize(builder = MessengerEvent.Builder.class)
public interface MessengerEvent {

    /**
     * Designates the source of where the message was sent from (ie: page for DrunkrMe FB page)
     */
    String object();

    /**
     * Contains a batched set of messages from users sent to the DrunkrMe page bot.
     * <pre>
     * - Each entry contains an array messaging which will always contain 1 entry.
     * -- Messaging contains sender.id and message for text
     * -- Messaging contains sender.id and postback for custom postback selections by user (not used yet)
     * </pre>
     *
     * @return {@link String}
     */
    List<Map<String, Object>> entry();

    class Builder extends MessengerEvent_Builder {

    }
}
