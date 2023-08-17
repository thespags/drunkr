package net.spals.drunkr.api;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.common.PhoneNumbers;
import net.spals.drunkr.model.ApiError;

/**
 * API that exposes common library functions across the app.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("commons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommonsResource {

    @Inject
    CommonsResource() {
    }

    @GET
    @Path("parsePhoneNumber")
    public Response parsePhoneNumber(
        @QueryParam("phoneNumber") final String phoneNumber,
        @QueryParam("defaultRegion") final Optional<String> defaultRegion
    ) {
        final Optional<String> phoneNumberAsE164 = PhoneNumbers.parse(phoneNumber, defaultRegion.orElse("US"));

        return phoneNumberAsE164.map(
            x -> Response.status(OK)
                .entity(ImmutableMap.of("phoneNumber", x))
                .build()
        ).orElseGet(
            () -> ApiError.newError(NOT_FOUND, "invalid phone number and region")
                .asResponseBuilder()
                .build()
        );
    }
}
