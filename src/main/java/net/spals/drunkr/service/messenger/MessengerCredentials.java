package net.spals.drunkr.service.messenger;

/**
 * Credentials for Facebook Messenger to verify webhook access and send responses through Messenger.
 *
 * @author jbrock
 */
public interface MessengerCredentials {

    String getVerifyToken();

    String getPageAccessToken();
}
