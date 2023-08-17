package net.spals.drunkr.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.messenger.MessengerClient;
import net.spals.drunkr.service.messenger.MessengerCredentials;

/**
 * REST api for interacting with Facebook Messenger.
 * <p>
 * POST or GET or /messenger/webhook
 *
 * @author jbrock
 */
@AutoBindSingleton
@Path(value = "messenger")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessengerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerResource.class);
    private static final String SUBSCRIBE = "subscribe";
    private static final String PAGE = "page";
    private static final String SENDER = "sender";
    private static final String TEXT = "text";
    private static final String POSTBACK = "postback";
    private static final String ID = "id";
    private static final String MESSAGE = "message";
    private static final String MESSAGING = "messaging";
    private final MessengerClient client;
    private final MessengerCredentials credentials;
    private final TextBasedParser parser;

    @Inject
    MessengerResource(
        final MessengerClient client,
        final MessengerCredentials credentials,
        final TextBasedParser parser
    ) {
        this.client = client;
        this.credentials = credentials;
        this.parser = parser;
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("webhook")
    @Consumes("application/json")
    public Response post(@NotNull final MessengerEvent event) {
        LOGGER.info("POST: " + event.toString());
        if (PAGE.equalsIgnoreCase(event.object())) {
            int status = 200;
            for (final Map<String, Object> entry : event.entry()) {
                final Map<String, Object> messaging = (Map<String, Object>) ((List<Object>) entry.get(MESSAGING)).get(0);
                final Map<String, String> sender = (Map<String, String>) messaging.get(SENDER);
                if (messaging.containsKey(MESSAGE)) {
                    final Map<String, Object> message = (Map<String, Object>) messaging.get(MESSAGE);
                    status = handleMessage(sender.get(ID), message);
                } else if (messaging.containsKey(POSTBACK)) {
                    // Currently postback is not configured to do anything.
                    final Map<String, Object> postback = (Map<String, Object>) messaging.get(POSTBACK);
                    LOGGER.info("Messenger Webhook POST Postback: " + postback.get(TEXT));
                    status = 200;
                }
            }
            return Response.status(status).build();
        } else {
            LOGGER.warn("POST request to Messenger resource not set as PAGE");
            return Response.status(404).build();
        }
    }

    private int handleMessage(final String senderId, final Map<String, Object> payload) {
        if (payload.containsKey(TEXT)) {
            final String textBody = (String) payload.get(TEXT);
            LOGGER.info("Messenger Webhook POST Text: " + senderId + ". Payload: " + payload);
            final String message = parser.parse(Source.MESSENGER, senderId, textBody);
            return client.sendMessage(buildResponseToSender(senderId, message));
        }
        LOGGER.info("Bad Messenger Webhook POST Text: " + senderId + ". Payload: " + payload);
        return 404;
    }

    private MessengerResponse buildResponseToSender(final String senderId, final String message) {
        return new MessengerResponse.Builder()
            .putRecipient(ID, senderId)
            .putMessage(TEXT, message)
            .messagingType(MessengerResponse.MessageType.RESPONSE)
            .build();
    }

    @GET
    @Path("webhook")
    public Response get(
        @QueryParam(value = "hub.mode") final String mode,
        @QueryParam(value = "hub.verify_token") final String verifyToken,
        @QueryParam(value = "hub.challenge") final String challenge
    ) {
        if (Objects.equals(SUBSCRIBE, mode) && Objects.equals(credentials.getVerifyToken(), verifyToken)) {
            LOGGER.info("Messenger Webhook GET: subscription success");
            return Response.ok(challenge).build();
        } else {
            LOGGER.info("Messenger Webhook GET: subscription failure. Token: " + verifyToken + ". Mode: " + mode);
            return Response.status(403).build();
        }
    }
}