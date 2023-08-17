package net.spals.drunkr.service.messenger;

import javax.validation.constraints.NotNull;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.netflix.governator.annotations.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindProvider;

/**
 * Provides {@link MessengerCredentials} based on the Messenger config setting.
 * If set to "prod" will use production credentials, otherwise defaults to test credentials.
 *
 * @author spags
 * @author jbrock
 */
@AutoBindProvider
class MessengerCredentialProvider implements Provider<MessengerCredentials> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerCredentialProvider.class);
    private final Map<String, MessengerCredentials> credentialMap;
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("messenger.env")
    private String environment = "test";

    @Inject
    MessengerCredentialProvider(final Map<String, MessengerCredentials> credentialMap) {
        this.credentialMap = credentialMap;
    }

    @Override
    public MessengerCredentials get() {
        LOGGER.info("Messenger credentials=" + environment);
        return credentialMap.get(environment);
    }
}