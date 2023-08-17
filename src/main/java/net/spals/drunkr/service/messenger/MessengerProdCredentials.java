package net.spals.drunkr.service.messenger;

import javax.validation.constraints.NotNull;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;

import net.spals.appbuilder.annotations.service.AutoBindInMap;

/**
 * Implementation of {@link MessengerCredentials} using Messenger production credentials.
 *
 * @author spags
 * @author jbrock
 */
@AutoBindInMap(baseClass = MessengerCredentials.class, key = "prod")
class MessengerProdCredentials implements MessengerCredentials {

    @NotNull
    @Configuration("messenger.verify.token")
    private String verifyToken;
    @NotNull
    @Configuration("messenger.page.token")
    private String pageToken;

    @Inject
    MessengerProdCredentials() {
    }

    @Override
    public String getVerifyToken() {
        return verifyToken;
    }

    @Override
    public String getPageAccessToken() {
        return pageToken;
    }
}
