package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.ImmutableMap;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.NotificationSubject;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;
import net.spals.drunkr.service.DrunkrJob;

/**
 * Unit tests for {@link JobStopCommand}.
 *
 * @author spags
 */
public class JobStopCommandTest {

    private static final Person DRINKER = Persons.SPAGS;
    private static final JobOptions JOB = new Builder()
        .userId(DRINKER.id())
        .source(Source.SMS)
        .build();
    @Mock
    private DatabaseService dbService;
    @Mock
    private ExecutorServiceFactory executorServiceFactory;
    @Mock
    private ExecutorService executorService;
    @Captor
    private ArgumentCaptor<ZonedDateTime> zonedDateTimeCaptor;
    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;
    private I18nSupport i18nSupport;
    private JobStopCommand command;
    private ImmutableMap<String, Object> request;
    private Key key;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new JobStopCommand(dbService, executorServiceFactory, i18nSupport);
        when(dbService.getJob(JOB.id().toHexString())).thenReturn(Optional.of(JOB));
        when(dbService.stopJob(eq(JOB), any())).thenReturn(true);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("job", JOB)
            .build();
        key = new Key.Builder(DrunkrJob.class)
            .addTags(DRINKER.id().toHexString())
            .build();
        when(executorServiceFactory.get(key)).thenReturn(Optional.of(executorService));
    }

    @Test
    public void noExecutorJob() {
        when(executorServiceFactory.get(key)).thenReturn(Optional.empty());

        final Response response = command.run(request);

        verifyStopped(response);
        verify(executorServiceFactory, never()).stop(key);
    }

    @Test
    public void executorAlreadyStopped() {
        when(executorService.isShutdown()).thenReturn(true);

        final Response response = command.run(request);

        verifyStopped(response);
        verify(executorServiceFactory, never()).stop(key);
    }

    @Test
    public void afterStopTime() {
        final JobOptions stoppedJob = new JobOptions.Builder()
            .mergeFrom(JOB)
            // subtract a bit to get a buffer for fast environments...
            .stopTime(ZonedDateTimes.nowUTC().minusMinutes(5))
            .build();
        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("job", stoppedJob)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.job.stop.fail", JOB.id()));
    }

    @Test
    public void stopSuccess() {
        final Response response = command.run(request);

        verifyStopped(response);
        verify(executorServiceFactory).stop(key);
        verify(dbService).insertNotification(notificationCaptor.capture());
        NotificationSubject.assertThat(notificationCaptor.getValue())
            .hasUserId(DRINKER.id())
            .hasEmptySourceUserId()
            .hasMessage(i18nSupport.getLabel("job.stopping.stopped", DRINKER.userName()));
    }

    private void verifyStopped(final Response response) {
        verify(dbService).stopJob(eq(JOB), zonedDateTimeCaptor.capture());
        final JobOptions updatedJob = new JobOptions.Builder()
            .mergeFrom(JOB)
            .stopTime(zonedDateTimeCaptor.getValue())
            .build();
        assertThat(response)
            .hasStatus(OK)
            .hasEntity(updatedJob);
    }

    @Test
    public void stopFail() {
        when(dbService.stopJob(eq(JOB), any())).thenReturn(false);

        final Response response = command.run(request);

        verify(executorServiceFactory).stop(key);
        verify(dbService).stopJob(eq(JOB), any());
        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.job.stop.fail", JOB.id()));
    }
}
