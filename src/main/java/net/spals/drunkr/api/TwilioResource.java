package net.spals.drunkr.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;

import com.twilio.twiml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.model.Source;

/**
 * REST API for interacting with twilio.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("twilio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TwilioResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioResource.class);
    private final TextBasedParser parser;

    @Inject
    TwilioResource(final TextBasedParser parser) {
        this.parser = parser;
    }

    @POST
    @Path("receive")
    @Consumes("application/x-www-form-urlencoded")
    public Response post(
        @FormParam("From") final String from,
        @FormParam("To") final String to,
        @FormParam("Body") final String body
    ) {
        final String message = parser.parse(Source.SMS, from, body);
        LOGGER.info("sending text response: " + message);
        return buildResponse(message);
    }

    private Response buildResponse(final String message) {
        final Message sms = new Message.Builder()
            .body(new Body(message))
            .build();
        final MessagingResponse response = new MessagingResponse.Builder()
            .message(sms)
            .build();
        return Response.ok(toXmlSafe(response), MediaType.APPLICATION_XML)
            .build();
    }

    private String toXmlSafe(final MessagingResponse response) {
        try {
            return response.toXml();
        } catch (final TwiMLException x) {
            throw new RuntimeException(x);
        }
    }
}
