package net.spals.drunkr.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.Person;

/**
 * REST API for interacting for authentication.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("users/{userIdNameOrPhone}/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinksResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersResource.class);
    private final Map<CommandType, ApiCommand> commands;

    @Inject
    LinksResource(final Map<CommandType, ApiCommand> commands) {
        this.commands = commands;
    }

    @POST
    @Path("untappd")
    public Response linkUntappd(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final String userName
    ) {
        LOGGER.info("POST: link for untappd: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("userName", userName)
            .build();
        return commands.get(CommandType.LINK_UNTAPPD).run(request);
    }

    @POST
    @Path("phoneRequest")
    public Response linkPhoneRequest(
        @PathParam("userIdNameOrPhone") final Person user,
        @QueryParam("defaultRegion") final Optional<String> defaultRegion,
        @NotNull final String phoneNumber
    ) {
        LOGGER.info("POST: link phone for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("phoneNumber", phoneNumber)
            .put("defaultRegion", defaultRegion)
            .build();
        return commands.get(CommandType.LINK_PHONE_REQUEST).run(request);
    }

    @POST
    @Path("phoneAuth")
    public Response linkPhoneAuth(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final String code
    ) {
        LOGGER.info("POST: link phone for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("code", code)
            .build();
        return commands.get(CommandType.LINK_PHONE_AUTH).run(request);
    }
}
