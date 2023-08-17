package net.spals.drunkr.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around {@link Runnable} to avoid swallowing error messages. Java's {@link Runnable} will silently
 * error leaving you confused.
 *
 * @author spags
 */
public class RunnableWrapper implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableWrapper.class);
    private final Runnable delegate;

    private RunnableWrapper(final Runnable delegate) {
        this.delegate = delegate;
    }

    public static Runnable wrap(final Runnable task) {
        return new RunnableWrapper(task);
    }

    @Override
    public void run() {
        try {
            delegate.run();
        } catch (final Throwable x) {
            LOGGER.info("error in runnable", x);
            throw x;
        }
    }
}