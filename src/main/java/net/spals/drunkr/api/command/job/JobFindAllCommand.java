package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.JobOptions;
import net.spals.drunkr.model.Person;

/**
 * Find all {@link JobOptions}'s associated to a user.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "JOB_FIND_ALL", keyType = CommandType.class)
class JobFindAllCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    JobFindAllCommand(
        final DatabaseService dbService
    ) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");

        // For now we return all jobs, we could add in date ranges to the api and db call in the future.
        final List<JobOptions> jobs = dbService.getJobs(person);

        return Response.status(OK)
            .entity(jobs)
            .build();
    }
}
