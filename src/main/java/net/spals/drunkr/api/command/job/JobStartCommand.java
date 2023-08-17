package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.RunnableWrapper;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.DrunkrJob;
import net.spals.drunkr.service.DrunkrJobFactory;

/**
 * Start an instance of {@link DrunkrJob}.
 *
 * @author spags
 * @author jbrock
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "JOB_START", keyType = CommandType.class)
class JobStartCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final DrunkrJobFactory jobFactory;
    private final ExecutorServiceFactory executorServiceFactory;
    private final I18nSupport i18nSupport;
    @SuppressWarnings("FieldMayBeFinal")
    @Configuration("job.period")
    private long period = 60;

    @Inject
    JobStartCommand(
        final DatabaseService dbService,
        final DrunkrJobFactory jobFactory,
        final ExecutorServiceFactory executorServiceFactory,
        final I18nSupport i18nSupport
    ) {
        this.executorServiceFactory = executorServiceFactory;
        this.jobFactory = jobFactory;
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        final JobOptions job = (JobOptions) request.get("jobOptions");

        if (job.stopTime().isPresent()) {
            return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.begin.invalid"))
                .asResponseBuilder()
                .build();
        }

        // Create a key based on task and user's username for a unique key.
        // This means our job is polled and stopped via the same key.
        final Key key = new Key.Builder(DrunkrJob.class)
            .addTags(person.id().toHexString())
            .build();
        if (executorServiceFactory.get(key).map(ExecutorService::isShutdown).orElse(true)) {
            final boolean added = dbService.insertJob(job);
            if (!added) {
                return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.begin.fail"))
                    .asResponseBuilder()
                    .build();
            }
            final ZonedDateTime now = ZonedDateTimes.nowUTC();
            final long delay = Math.max(0, ChronoUnit.SECONDS.between(now, job.startTime()));

            final ScheduledExecutorService executor = executorServiceFactory.createSingleThreadScheduledExecutor(key);
            executor.scheduleAtFixedRate(
                RunnableWrapper.wrap(jobFactory.createJob(job)),
                delay,
                job.period().orElse(period),
                TimeUnit.SECONDS
            );
            return Response.status(OK)
                .entity(job)
                .build();
        }
        return ApiError.newError(CONFLICT, i18nSupport.getLabel("command.begin.duplicate"))
            .asResponseBuilder()
            .build();
    }
}
