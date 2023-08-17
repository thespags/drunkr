package net.spals.drunkr.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.Person;

/**
 * API for follower actions.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("users/{userIdNameOrPhone}/followers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FollowersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersResource.class);
    private final Map<CommandType, ApiCommand> commands;

    @Inject
    FollowersResource(final Map<CommandType, ApiCommand> commands) {
        this.commands = commands;
    }

    @GET
    public Response allFollowers(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("GET: allFollowers for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        return commands.get(CommandType.FOLLOWER_LIST).run(request);
    }

    @POST
    public Response addFollower(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final Map<String, Object> payload
    ) {
        LOGGER.info("POST: addFollower for user: " + user.id() + " with payload: " + payload);
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .putAll(payload)
            .build();
        return commands.get(CommandType.FOLLOWER_INVITE).run(request);
    }

    @DELETE
    @Path("{targetUserIdNameOrPhone}")
    public Response removeFollower(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("targetUserIdNameOrPhone") final Person targetUser
    ) {
        LOGGER.info("DELETE: removeFollower for user: " + user.id() + " with target id: " + targetUser.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("targetUser", targetUser)
            .build();
        return commands.get(CommandType.FOLLOWER_REMOVE).run(request);
    }
}
