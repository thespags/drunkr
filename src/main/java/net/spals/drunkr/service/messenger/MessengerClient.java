package net.spals.drunkr.service.messenger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

import com.google.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.common.CloseableClient;
import net.spals.drunkr.model.MessengerResponse;

/**
 * A client that sends Messenger responses through Facebook.
 *
 * @author jbrock
 */
@AutoBindSingleton
public class MessengerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerClient.class);
    private static final String URL = "https://graph.facebook.com/v2.6/me/messages";
    private static final String ACCESS_TOKEN = "access_token";
    private final MessengerCredentials credentials;
    private final ObjectMapper mapper;

    @Inject
    MessengerClient(final MessengerCredentials credentials, final ObjectMapper mapper) {
        this.credentials = credentials;
        this.mapper = mapper;
    }

    public int sendMessage(final MessengerResponse response) {
        try (final CloseableClient client = CloseableClient.newClient()) {
            final String responseBody = mapper.writeValueAsString(response);
            final UriBuilder uriBuilder = UriBuilder.fromUri(URL)
                .queryParam(ACCESS_TOKEN, credentials.getPageAccessToken());
            try (
                final Response postResponse = client.getClient()
                    .target(uriBuilder)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(responseBody))
            ) {
                LOGGER.info("Messenger client response=" + postResponse);
                return postResponse.getStatus();
            }
        } catch (final Throwable x) {
            LOGGER.info("Messenger client failed to post", x);
            return 404;
        }
    }
}
