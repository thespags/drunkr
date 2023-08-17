package net.spals.drunkr.service.twilio;

import javax.validation.constraints.NotNull;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.netflix.governator.annotations.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindProvider;

/**
 * Provides {@link TwilioCredentials} based on the "twilio.env" config setting.
 * If set to "prod" will use production credentials, otherwise defaults to test credentials.
 *
 * @author spags
 */
@AutoBindProvider
class TwilioCredentialProvider implements Provider<TwilioCredentials> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioCredentialProvider.class);
    private final Map<String, TwilioCredentials> credentialMap;
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("twilio.env")
    private String environment = "test";

    @Inject
    TwilioCredentialProvider(final Map<String, TwilioCredentials> credentialMap) {
        this.credentialMap = credentialMap;
    }

    @Override
    public TwilioCredentials get() {
        LOGGER.info("Twilio credentials=" + environment);
        return credentialMap.get(environment);
    }
}
