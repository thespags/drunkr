package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key.Builder;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.DrunkrJob;

/**
 * Stops but does not remove the {@link JobOptions}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "JOB_STOP", keyType = CommandType.class)
class JobStopCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final ExecutorServiceFactory executorServiceFactory;
    private final I18nSupport i18nSupport;

    @Inject
    JobStopCommand(
        final DatabaseService dbService,
        final ExecutorServiceFactory executorServiceFactory,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.executorServiceFactory = executorServiceFactory;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        final JobOptions job = (JobOptions) request.get("job");

        final Key key = new Builder(DrunkrJob.class)
            .addTags(person.id().toHexString())
            .build();
        final Optional<ExecutorService> executor = executorServiceFactory.get(key);

        final boolean isShutdown = executor.map(ExecutorService::isShutdown).orElse(true);

        // Shutdown the job now or report an error if it wasn't running.
        if (!isShutdown) {
            executorServiceFactory.stop(key);
        }

        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final boolean isStopped = !job.stopTime().map(x -> ZonedDateTimes.isOnOrAfter(x, now)).orElse(true);

        // Now see if we need to update the stop time, checking if it was already stopped.
        if (isStopped) {
            return ApiError.newError(CONFLICT, i18nSupport.getLabel("command.job.stop.fail", job.id()))
                .asResponseBuilder()
                .build();
        }

        final boolean stopped = dbService.stopJob(job, now);

        if (stopped) {
            final JobOptions stoppedJob = new JobOptions.Builder()
                .mergeFrom(job)
                .stopTime(now)
                .build();

            final Notification notification = new Notification.Builder()
                .userId(person.id())
                .message(i18nSupport.getLabel("job.stopping.stopped", person.userName()))
                .source(job.source())
                .timestamp(now)
                .build();
            dbService.insertNotification(notification);

            return Response.status(OK)
                .entity(stoppedJob)
                .build();
        }

        return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.job.stop.fail", job.id()))
            .asResponseBuilder()
            .build();
    }
}
