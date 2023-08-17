package net.spals.drunkr.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.Invite;
import net.spals.drunkr.model.Person;

/**
 * REST API for interacting with Users and all associated actions / entities / operations.
 *
 * @author jbrock
 */
@AutoBindSingleton
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersResource.class);
    private final Map<CommandType, ApiCommand> commands;

    @Inject
    UsersResource(final Map<CommandType, ApiCommand> commands) {
        this.commands = commands;
    }

    @GET
    public Response allUsers() {
        LOGGER.info("GET: allUsers");
        return commands.get(CommandType.USER_FIND_ALL).run(ImmutableMap.of());
    }

    @POST
    public Response createUser(@NotNull final Person user) {
        LOGGER.info("POST: createUser: " + user);
        final Map<String, Object> request = ImmutableMap.of("user", user);
        return commands.get(CommandType.USER_ADD).run(request);
    }

    @GET
    @Path("{userIdNameOrPhone}")
    public Response getUser(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("GET: getUser: " + user.id());
        final Map<String, Object> request = ImmutableMap.of("user", user);
        return commands.get(CommandType.USER_FIND).run(request);
    }

    @PATCH
    @Path("{userIdNameOrPhone}")
    public Response updateUser(
        @PathParam("userIdNameOrPhone") final Person user,
        final Map<String, Object> payload
    ) {
        LOGGER.info("PATCH: updateUser: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("payload", payload)
            .build();
        return commands.get(CommandType.USER_UPDATE).run(request);
    }

    @POST
    @Path("{userIdNameOrPhone}/invite")
    public Response invite(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final Invite invite
    ) {
        LOGGER.info("POST: invite for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("invite", invite)
            .build();
        return commands.get(CommandType.USER_INVITE).run(request);
    }

    @GET
    @Path("{userIdNameOrPhone}/bac")
    public Response checkBac(
        @PathParam("userIdNameOrPhone") final Person user,
        @QueryParam("from") final Optional<ZonedDateTime> fromDateTime,
        @QueryParam("to") final Optional<ZonedDateTime> toDateTime
    ) {
        final String logMessage = "GET: bac for user: " + user.id() +
            " fromTime: " + fromDateTime +
            " toTime: " + toDateTime;
        LOGGER.info(logMessage);
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("from", fromDateTime)
            .put("to", toDateTime)
            .build();
        return commands.get(CommandType.USER_CHECK_BAC).run(request);
    }

    @GET
    @Path("{userIdNameOrPhone}/leaders")
    public Response leaders(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        final String logMessage = "GET: leader board for user: " + user.id();
        LOGGER.info(logMessage);
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        return commands.get(CommandType.USER_LEADERS).run(request);
    }

    @GET
    @Path("{userIdNameOrPhone}/auth")
    public Response auth(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("GET: auth for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        return commands.get(CommandType.USER_AUTH_REQUEST).run(request);
    }

    @POST
    @Path("{userIdNameOrPhone}/auth")
    public Response auth(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final String code
    ) {
        LOGGER.info("GET: auth for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("code", code)
            .build();
        return commands.get(CommandType.USER_AUTH).run(request);
    }
}
