package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.RunnableWrapper;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;
import net.spals.drunkr.service.DrunkrJob;
import net.spals.drunkr.service.DrunkrJobFactory;

/**
 * Unit tests for {@link JobStartCommand}.
 *
 * @author spags
 */
public class JobStartCommandTest {

    private static final Person DRINKER = Persons.SPAGS;
    private static final JobOptions JOB = new Builder()
        .userId(DRINKER.id())
        .source(Source.SMS)
        .build();
    private static final JobOptions STOPPED_JOB = new Builder()
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
    private I18nSupport i18nSupport;
    private JobStartCommand command;
    private ImmutableMap<String, Object> request;
    private Key key;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new JobStartCommand(dbService, taskFactory, executorServiceFactory, i18nSupport);
        when(dbService.insertJob(JOB)).thenReturn(true);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("jobOptions", JOB)
            .build();
        key = new Key.Builder(DrunkrJob.class)
            .addTags(DRINKER.id().toHexString())
            .build();
        when(executorServiceFactory.get(key)).thenReturn(Optional.of(executorService));
        when(executorServiceFactory.createSingleThreadScheduledExecutor(key)).thenReturn(executor);
    }

    @Test
    public void startJobWithNoExecutorJob() {
        when(executorServiceFactory.get(key)).thenReturn(Optional.empty());

        final Response response = command.run(request);

        verify(executor).scheduleAtFixedRate(any(RunnableWrapper.class), eq(0L), eq(60L), eq(TimeUnit.SECONDS));
        verify(executorServiceFactory).createSingleThreadScheduledExecutor(key);
        assertThat(response)
            .hasStatus(OK)
            .hasEntity(JOB);
    }

    @Test
    public void startJobWithExecutorShutdown() {
        when(executorService.isShutdown()).thenReturn(true);

        final Response response = command.run(request);

        verify(executor).scheduleAtFixedRate(any(RunnableWrapper.class), eq(0L), eq(60L), eq(TimeUnit.SECONDS));
        verify(executorServiceFactory).createSingleThreadScheduledExecutor(key);
        assertThat(response)
            .hasStatus(OK)
            .hasEntity(JOB);
    }

    @Test
    public void jobAlreadyStarted() {
        when(executorService.isShutdown()).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.begin.duplicate"));
    }

    @Test
    public void insertJobFail() {
        when(dbService.insertJob(JOB)).thenReturn(false);
        when(executorService.isShutdown()).thenReturn(true);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.begin.fail"));
    }

    @Test
    public void invalidJobWithStopTime() {
        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("jobOptions", STOPPED_JOB)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.begin.invalid"));
    }
}