package net.spals.drunkr.model;

import javax.ws.rs.core.Response;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

/**
 * A bean to hold information for API Error Responses.
 *
 * @author tkral
 */
@AutoValue
public abstract class ApiError {

    public static ApiError newError(
        final Response.Status status,
        final String message
    ) {
        return new AutoValue_ApiError(message, status);
    }

    public final Response.ResponseBuilder asResponseBuilder() {
        return Response.status(status())
            .entity(ImmutableMap.of("message", message()));
    }

    public abstract String message();

    public abstract Response.Status status();
}
