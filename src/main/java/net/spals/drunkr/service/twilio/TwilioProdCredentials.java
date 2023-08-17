package net.spals.drunkr.service.twilio;

import javax.validation.constraints.NotNull;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;

import net.spals.appbuilder.annotations.service.AutoBindInMap;

/**
 * Implementation of {@link TwilioCredentials} using Twilio production credentials.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = TwilioCredentials.class, key = "prod")
class TwilioProdCredentials implements TwilioCredentials {

    @NotNull
    @Configuration("twilio.prod.account.sid")
    private String accountSid;
    @NotNull
    @Configuration("twilio.prod.auth.token")
    private String authToken;
    @NotNull
    @Configuration("twilio.prod.phone.number")
    private String phoneNumber;

    @Inject
    TwilioProdCredentials() {
    }

    @Override
    public String getAccountSid() {
        return accountSid;
    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }
}
