package net.spals.drunkr.service.messenger;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;

/**
 * Test implementation of {@link MessengerCredentials} using Messenger fake credentials.
 *
 * @author spags
 * @author jbrock
 */
@AutoBindInMap(baseClass = MessengerCredentials.class, key = "test")
class MessengerTestCredentials implements MessengerCredentials {

    @Inject
    MessengerTestCredentials() {
    }

    @Override
    public String getVerifyToken() {
        return "Foo";
    }

    @Override
    public String getPageAccessToken() {
        return "Bar";
    }
}
