package net.spals.drunkr.startup;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.RunnableWrapper;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.service.DrunkrJob;
import net.spals.drunkr.service.DrunkrJobFactory;

/**
 * Starts jobs up after a dyno restart, finding all jobs with empty stop times or stop times after now.
 *
 * @author spags
 */
@AutoBindSingleton
class JobStartup {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStartup.class);
    private final DatabaseService dbService;
    private final DrunkrJobFactory jobFactory;
    private final ExecutorServiceFactory executorServiceFactory;
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("job.period")
    private long period = 60;

    @Inject
    JobStartup(
        final DatabaseService dbService,
        final DrunkrJobFactory jobFactory,
        final ExecutorServiceFactory executorServiceFactory
    ) {
        this.dbService = dbService;
        this.jobFactory = jobFactory;
        this.executorServiceFactory = executorServiceFactory;
    }

    @PostConstruct
    void start() {
        LOGGER.info("restarting all jobs");
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        dbService.allRunningJobs(now).forEach(
            job -> {
                final Key key = new Key.Builder(DrunkrJob.class)
                    .addTags(job.userId().toHexString())
                    .build();
                if (executorServiceFactory.get(key).map(ExecutorService::isShutdown).orElse(true)) {
                    LOGGER.info("Starting job for for user on startup: " + job.userId());
                    final long delay = Math.max(0, ChronoUnit.SECONDS.between(now, job.startTime()));

                    final ScheduledExecutorService executor = executorServiceFactory
                        .createSingleThreadScheduledExecutor(key);
                    executor.scheduleAtFixedRate(
                        RunnableWrapper.wrap(jobFactory.createJob(job)),
                        delay,
                        job.period().orElse(period),
                        TimeUnit.SECONDS
                    );
                } else {
                    LOGGER.info("Job already started for user on startup: " + job.userId());
                }
            }
        );
    }
}
