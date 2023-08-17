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
import net.spals.drunkr.model.JobOptions;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.service.DrunkrJob;

/**
 * As of now we aren't persisting the job into the database.
 * A user can have at most one job, {@link DrunkrJob}.
 * This is the REST API that controls starting, stopping, and getting the results of the task.
 *
 * @author spags
 */
@AutoBindSingleton
@Path("users/{userIdNameOrPhone}/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobsResource.class);
    private final Map<CommandType, ApiCommand> commands;

    @Inject
    JobsResource(final Map<CommandType, ApiCommand> apiCommands) {
        commands = apiCommands;
    }

    @POST
    public Response startJob(
        @PathParam("userIdNameOrPhone") final Person user,
        @NotNull final JobOptions jobOptions
    ) {
        LOGGER.info("POST: addJob for user: " + user.id() + " job: " + jobOptions);
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("jobOptions", jobOptions)
            .build();
        final Response response = commands.get(CommandType.JOB_START).run(request);
        LOGGER.info("END POST: addJob for user: " + user.id() + "=" + response);
        return response;
    }

    @GET
    public Response allJobs(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("GET: allJobs for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        final Response response = commands.get(CommandType.JOB_FIND_ALL).run(request);
        LOGGER.info("END GET: allJobs for user: " + user.id());
        return response;
    }

    @GET
    @Path("check")
    public Response checkJob(
        @PathParam("userIdNameOrPhone") final Person user
    ) {
        LOGGER.info("GET: checkJob for user: " + user.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();
        final Response response = commands.get(CommandType.JOB_CHECK).run(request);
        LOGGER.info("END GET: checkJob for user: " + user.id() + "=" + response);
        return response;
    }

    @GET
    @Path("{jobId}")
    public Response getJob(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("jobId") final JobOptions job
    ) {
        LOGGER.info("GET: getJob for user: " + user.id() + " jobId: " + job.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("job", job)
            .build();
        final Response response = commands.get(CommandType.JOB_FIND).run(request);
        LOGGER.info("END GET: getJob for user: " + user.id() + " jobId: " + job.id() + "=" + response);
        return response;
    }

    @DELETE
    @Path("{jobId}")
    public Response stopJob(
        @PathParam("userIdNameOrPhone") final Person user,
        @PathParam("jobId") final JobOptions job
    ) {
        LOGGER.info("POST: stopJob for user: " + user.id() + " jobId: " + job.id());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .put("job", job)
            .build();
        final Response response = commands.get(CommandType.JOB_STOP).run(request);
        LOGGER.info("END POST: stopJob for user: " + user.id() + " jobId: " + job.id() + "=" + response);
        return response;
    }
}