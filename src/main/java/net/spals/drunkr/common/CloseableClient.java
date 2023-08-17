package net.spals.drunkr.common;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Waiting for this <a href='https://github.com/jax-rs/api/issues/472'>issue</a> to be resolved.
 *
 * @author spags
 */
public class CloseableClient implements AutoCloseable {

    private final Client client;

    private CloseableClient(final Client client) {
        this.client = client;
    }

    public static CloseableClient newClient() {
        return newClient(ClientBuilder.newClient());
    }

    public static CloseableClient newClient(final Client client) {
        return new CloseableClient(client);
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void close() {
        client.close();
    }
}
