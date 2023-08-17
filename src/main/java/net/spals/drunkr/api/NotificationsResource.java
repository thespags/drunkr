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
import net.spals.drunkr.model.Notification;
import net.spals.drunkr.model.Person;

/**
 * REST API for interacting with users and their notifications.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("users/{userIdNameOrPhone}/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsResource.class);
    private final Map<CommandType, ApiCommand> commands;

    @Inject
    NotificationsResource(final Map<CommandType, ApiCommand> commands) {
        this.commands = commands;
    }

    @POST
    public Response addNotification(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final Notification notification
    ) {
        LOGGER.info("POST: addNotification for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("notification", notification)
            .build();
        return commands.get(CommandType.NOTIFICATION_ADD).run(request);
    }

    @GET
    public Response allNotifications(
        @PathParam("userIdNameOrPhone") final Person user,
        @QueryParam("from") final Optional<ZonedDateTime> fromDateTime,
        @QueryParam("to") final Optional<ZonedDateTime> toDateTime

    ) {
        LOGGER.info("GET: allNotifications for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("from", fromDateTime)
            .put("to", toDateTime)
            .build();
        return commands.get(CommandType.NOTIFICATION_FIND_ALL).run(request);
    }

    @GET
    @Path("{notificationId}")
    public Response getNotification(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("notificationId") final Notification notification
    ) {
        LOGGER.info("GET: getNotification for user: " + user.id() + " notificationId: " + notification.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("notification", notification)
            .build();
        return commands.get(CommandType.NOTIFICATION_FIND).run(request);
    }

    @PATCH
    @Path("{notificationId}")
    public Response updateNotification(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("notificationId") final Notification notification,
        @NotNull final Map<String, Object> payload
    ) {
        LOGGER.info("PATCH: updateNotification for user: " + user.id() + " notificationId: " + notification.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("notification", notification)
            .put("payload", payload)
            .build();
        return commands.get(CommandType.NOTIFICATION_UPDATE).run(request);
    }

    @POST
    @Path("readAll")
    public Response readAllNotifications(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("POST: readAllNotifications for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        return commands.get(CommandType.NOTIFICATION_READ_ALL).run(request);
    }
}
