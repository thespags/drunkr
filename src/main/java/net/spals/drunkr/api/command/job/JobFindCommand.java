package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.JobOptions;

/**
 * Find a specific {@link JobOptions} given an id.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "JOB_FIND", keyType = CommandType.class)
class JobFindCommand implements ApiCommand {

    @Inject
    JobFindCommand() {
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final JobOptions job = (JobOptions) request.get("job");
        return Response.status(OK)
            .entity(job)
            .build();
    }
}
