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
import net.spals.drunkr.model.Checkin;
import net.spals.drunkr.model.Person;

/**
 * REST API for interacting with users and their checkins.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("users/{userIdNameOrPhone}/checkins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CheckinsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckinsResource.class);
    private final Map<CommandType, ApiCommand> commands;

    @Inject
    CheckinsResource(final Map<CommandType, ApiCommand> commands) {
        this.commands = commands;
    }

    @POST
    public Response addCheckin(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final Checkin checkin
    ) {
        LOGGER.info("POST: addCheckin for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("checkin", checkin)
            .build();
        return commands.get(CommandType.CHECKIN_ADD).run(request);
    }

    @GET
    public Response allCheckins(
        @PathParam("userIdNameOrPhone") final Person user,
        @QueryParam("from") final Optional<ZonedDateTime> fromDateTime,
        @QueryParam("to") final Optional<ZonedDateTime> toDateTime

    ) {
        LOGGER.info("GET: allCheckins for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("from", fromDateTime)
            .put("to", toDateTime)
            .build();
        return commands.get(CommandType.CHECKIN_FIND_ALL).run(request);
    }

    @GET
    @Path("{checkinId}")
    public Response getCheckin(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("checkinId") final Checkin checkin
    ) {
        LOGGER.info("GET: getCheckin for user: " + user.id() + " checkinId: " + checkin.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("checkin", checkin)
            .build();
        return commands.get(CommandType.CHECKIN_FIND).run(request);
    }

    @PATCH
    @Path("{checkinId}")
    public Response updateCheckin(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("checkinId") final Checkin checkin,
        @NotNull final Map<String, Object> payload
    ) {
        LOGGER.info("PATCH: updateCheckin for user: " + user.id() + " checkinId: " + checkin.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("checkin", checkin)
            .put("payload", payload)
            .build();
        return commands.get(CommandType.CHECKIN_UPDATE).run(request);
    }

    @DELETE
    @Path("{checkinId}")
    public Response removeCheckin(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("checkinId") final Checkin checkin
    ) {
        LOGGER.info("DELETE: removeCheckin for user: " + user.id() + " checkinId: " + checkin.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("checkin", checkin)
            .build();
        return commands.get(CommandType.CHECKIN_REMOVE).run(request);
    }

    @DELETE
    public Response removeAllCheckins(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("DELETE: removeAllCheckins for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        return commands.get(CommandType.CHECKIN_REMOVE_ALL).run(request);
    }
}
