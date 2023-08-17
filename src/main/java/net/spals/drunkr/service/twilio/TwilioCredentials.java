package net.spals.drunkr.service.twilio;

/**
 * Interface for authenticating with Twilio.
 *
 * @author spags
 */
interface TwilioCredentials {

    String getAccountSid();

    String getAuthToken();

    String getPhoneNumber();
}
