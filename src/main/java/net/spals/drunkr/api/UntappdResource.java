package net.spals.drunkr.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;

/**
 * REST API for associating drunkr users, untappd user names, and access tokens.
 * <p>
 * POST { "untappdUserName" : "userName", "accessToken" : "token" }/untappd/link
 * *
 * @author jbrock
 */
@AutoBindSingleton
@Path("untappd")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UntappdResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UntappdResource.class);

    private final Map<CommandType, ApiCommand> commands;

    @Inject
    UntappdResource(final Map<CommandType, ApiCommand> commands) {
        this.commands = commands;
    }

    @POST
    @Path("link")
    public Response post(@NotNull final Map<String, Object> request) {
        LOGGER.info("POST: attempting to LINK: " + request.get("untappdUserName"));
        return commands.get(CommandType.UNTAPPD_AUTH).run(request);
    }
}
