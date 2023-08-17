package net.spals.drunkr.service.twilio;

import javax.validation.constraints.NotNull;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;

import net.spals.appbuilder.annotations.service.AutoBindInMap;

/**
 * Implementation of {@link TwilioCredentials} using Twilio test credentials.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = TwilioCredentials.class, key = "test")
class TwilioTestCredentials implements TwilioCredentials {

    @NotNull
    @Configuration("twilio.test.account.sid")
    private String accountSid;
    @NotNull
    @Configuration("twilio.test.auth.token")
    private String authToken;
    @NotNull
    @Configuration("twilio.test.phone.number")
    private String phoneNumber;

    @Inject
    TwilioTestCredentials() {
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
