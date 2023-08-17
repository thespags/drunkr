package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.JobOptions;
import net.spals.drunkr.model.Person;

/**
 * Get the running {@link JobOptions} if the user has a {@link JobOptions} with a stop time after now or empty.
 * Otherwise empty.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "JOB_CHECK", keyType = CommandType.class)
class JobCheckCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    JobCheckCommand(
        final DatabaseService dbService
    ) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        final Optional<JobOptions> runningJob = dbService.getRunningJob(person, ZonedDateTimes.nowUTC());
        return Response.status(OK)
            .entity(runningJob)
            .build();
    }
}
