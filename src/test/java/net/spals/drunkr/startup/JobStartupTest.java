package net.spals.drunkr.startup;

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableList;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.RunnableWrapper;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;
import net.spals.drunkr.service.DrunkrJob;
import net.spals.drunkr.service.DrunkrJobFactory;

/**
 * Unit tests for {@link JobStartup}.
 *
 * @author spags
 */
public class JobStartupTest {

    private static final Person DRINKER = Persons.SPAGS;
    private static final JobOptions JOB = new Builder()
        .userId(DRINKER.id())
        .source(Source.SMS)
        .stopTime(ZonedDateTimes.nowUTC())
        .build();
    @Mock
    private DatabaseService dbService;
    @Mock
    private DrunkrJobFactory taskFactory;
    @Mock
    private ExecutorServiceFactory executorServiceFactory;
    @Mock
    private ExecutorService executorService;
    @Mock
    private ScheduledExecutorService executor;
    private JobStartup startup;
    private Key key;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        startup = new JobStartup(dbService, taskFactory, executorServiceFactory);

        key = new Key.Builder(DrunkrJob.class)
            .addTags(DRINKER.id().toHexString())
            .build();
        when(executorServiceFactory.createSingleThreadScheduledExecutor(key)).thenReturn(executor);
    }

    private void verifyNoJobEnqueued() {
        verify(executor, never()).scheduleAtFixedRate(
            any(RunnableWrapper.class),
            eq(0L),
            eq(900L),
            eq(TimeUnit.SECONDS)
        );
        verify(executorServiceFactory, never()).createSingleThreadScheduledExecutor(key);
    }

    private void verifyJobEnqueued() {
        verify(executor).scheduleAtFixedRate(any(RunnableWrapper.class), eq(0L), eq(60L), eq(TimeUnit.SECONDS));
        verify(executorServiceFactory).createSingleThreadScheduledExecutor(key);
    }

    @Test
    public void restartJob() {
        when(executorServiceFactory.get(key)).thenReturn(Optional.empty());
        when(dbService.allRunningJobs(any())).thenReturn(ImmutableList.of(JOB));

        startup.start();

        verifyJobEnqueued();
    }

    @Test
    public void noJobs() {
        when(executorServiceFactory.get(key)).thenReturn(Optional.empty());
        when(dbService.allRunningJobs(any())).thenReturn(ImmutableList.of());

        startup.start();

        verifyNoJobEnqueued();
    }

    @Test
    public void jobExecutorAlreadyRunning() {
        when(executorServiceFactory.get(key)).thenReturn(Optional.of(executorService));
        when(executorService.isShutdown()).thenReturn(false);
        when(dbService.allRunningJobs(any())).thenReturn(ImmutableList.of(JOB));

        startup.start();

        verifyNoJobEnqueued();
    }

    @Test
    public void jobExecutorShutdown() {
        when(executorServiceFactory.get(key)).thenReturn(Optional.empty());
        when(executorService.isShutdown()).thenReturn(true);
        when(dbService.allRunningJobs(any())).thenReturn(ImmutableList.of(JOB));

        startup.start();

        verifyJobEnqueued();
    }
}