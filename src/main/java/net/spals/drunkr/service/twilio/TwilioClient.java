package net.spals.drunkr.service.twilio;

import com.google.inject.Inject;

import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * A client that sends SMS messages over Twilio.
 *
 * @author spags
 */
@AutoBindSingleton
public class TwilioClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioClient.class);
    private final TwilioCredentials credentials;
    private final TwilioRestClient client;

    @Inject
    TwilioClient(final TwilioCredentials credentials, final TwilioRestClient client) {
        this.credentials = credentials;
        this.client = client;
    }

    @SuppressWarnings("UnusedReturnValue")
    public String sendMessage(final String phoneNumber, final String text) {
        try {
            final Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(credentials.getPhoneNumber()),
                text
            ).create(client);
            LOGGER.info("Sent phoneNumber=" + phoneNumber + " sid=" + message.getSid());
            return message.getSid();
        } catch (final Throwable x) {
            LOGGER.info("Error sending twilio message", x);
            return "";
        }
    }
}
