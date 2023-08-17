package net.spals.drunkr.service.twilio;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.twilio.http.TwilioRestClient;

import net.spals.appbuilder.annotations.service.AutoBindProvider;

/**
 * Creates a {@link TwilioRestClient} based on a {@link TwilioCredentials} provided by {@link TwilioRestClientProvider}.
 *
 * @author spags
 */
@AutoBindProvider
class TwilioRestClientProvider implements Provider<TwilioRestClient> {

    private final TwilioCredentials credentials;

    @Inject
    private TwilioRestClientProvider(final TwilioCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public TwilioRestClient get() {
        return new TwilioRestClient.Builder(
            credentials.getAccountSid(),
            credentials.getAuthToken()
        ).build();
    }
}
