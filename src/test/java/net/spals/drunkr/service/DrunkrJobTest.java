package net.spals.drunkr.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.*;

import static net.spals.drunkr.common.NotificationSubject.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.*;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.untappd.CheckinProvider;

/**
 * Unit tests for {@link DrunkrJob}.
 *
 * @author spags
 */
public class DrunkrJobTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final String UNTAPPD_USER_NAME = "untappdUsername";
    @Mock
    private DatabaseService dbService;
    @Mock
    private ExecutorServiceFactory executorServiceFactory;
    @Mock
    private FollowersNotifier notifier;
    @Mock
    private CheckinProvider checkinProvider;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;
    private I18nSupport i18nSupport;
    private UntappdLink link;
    private DrunkrJob task;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        i18nSupport = I18nSupports.getEnglish();
        task = createTask(ZonedDateTimes.nowUTC().minusMinutes(15), Optional.empty());
        link = new UntappdLink.Builder()
            .userId(DRUNK.id())
            .untappdName(UNTAPPD_USER_NAME)
            .build();
        when(dbService.getUntappdLink(any(Person.class))).thenReturn(Optional.of(link));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK));
    }

    /**
     * Assumes a simple drunk message where drink count and sub message are stable.
     */
    private String createBacMessage(final double bac, final String message, final int drinkCount) {
        return i18nSupport.getLabel("job.bac.state", DRUNK.userName(), bac, message, drinkCount);
    }

    private DrunkrJob createTask(
        final ZonedDateTime startTime,
        final Optional<ZonedDateTime> stopTime
    ) {
        final JobOptions options = new JobOptions.Builder()
            .userId(DRUNK.id())
            .startTime(startTime)
            .stopTime(stopTime)
            .source(Source.SMS)
            .build();
        final DrunkrJob drunkrJob = new DrunkrJob(
            new BacMessage(I18nSupports.getEnglish()),
            ImmutableMap.of("api", checkinProvider),
            dbService,
            executorServiceFactory,
            notifier,
            i18nSupport,
            options
        );
        drunkrJob.setCheckinProvider();
        return drunkrJob;
    }

    private Checkin createDuffCheckin() {
        return new Checkin.Builder()
            .userId(DRUNK.id())
            .name("Duff")
            .producer("Duff Brewery")
            .timestamp(ZonedDateTimes.nowUTC())
            .style(Style.DRAFT)
            .size(Style.DRAFT.getServingSize())
            .abv(.05)
            .build();
    }

    private Checkin createWineCheckin() {
        return new Checkin.Builder()
            .userId(DRUNK.id())
            .name("Wine")
            .timestamp(ZonedDateTimes.nowUTC())
            .style(Style.WINE)
            .size(Style.WINE.getServingSize())
            .abv(.14)
            .build();
    }

    @Test
    public void storeCheckins() {
        final List<Checkin> checkins = ImmutableList.of(createDuffCheckin());
        when(checkinProvider.get(any(), any())).thenReturn(checkins);
        when(dbService.insertCheckins(any())).thenReturn(true);
        final ZonedDateTime lastModified = task.getLastModified();

        task.run();

        verify(dbService).insertCheckins(checkins);
        verify(dbService).updateJob(any(), any());
        assertThat(task.getLastModified()).isAtLeast(lastModified);
    }

    @Test
    public void cancelJobIfSober() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of());

        task.run();

        verify(dbService).stopJob(any(), any());
        verify(executorServiceFactory).stop(any(Key.class));
        verify(notifier).notify(eq(DRUNK), messageCaptor.capture(), eq(Source.SMS), any());
        assertThat(messageCaptor.getValue())
            .isEqualTo(createBacMessage(0, i18nSupport.getLabel("level_sober"), 0));
        verify(dbService).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
            .hasUserId(DRUNK.id())
            .hasEmptySourceUserId()
            .hasMessage(i18nSupport.getLabel("job.stopping.sober", DRUNK.userName()))
            .isNotPushed();
    }

    @Test
    public void bufferJobIfSober() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of());
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        task = createTask(now, Optional.empty());

        task.run();

        verify(executorServiceFactory, never()).stop(any(Key.class));
        // Confirm negative buffer...
        verify(checkinProvider).get(eq(Optional.of(link)), eq(Optional.of(now)));
    }

    @Test
    public void cancelJobIfAfterStopTime() {
        // Even if we aren't sober, we cancel the job after the stop time.
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of(createDuffCheckin()));
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        task = createTask(now.minusMinutes(10), Optional.of(now.minusMinutes(5)));

        task.run();

        verify(executorServiceFactory).stop(any(Key.class));
        verify(notifier).notify(eq(DRUNK), messageCaptor.capture(), eq(Source.SMS), any());
        assertThat(messageCaptor.getValue())
            .isEqualTo(createBacMessage(0.03, i18nSupport.getLabel("level_lightheaded"), 1));
        verify(dbService).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
            .hasUserId(DRUNK.id())
            .hasEmptySourceUserId()
            .hasMessage(i18nSupport.getLabel("job.stopping.stopped", DRUNK.userName()))
            .isNotPushed();
    }

    @Test
    public void drunkrCheckinsUsed() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of());
        when(dbService.getCheckins(any(), any(), any()))
            .thenReturn(ImmutableList.of(createWineCheckin()));

        task.run();

        verify(dbService).insertBacCalculation(any(BacCalculation.class));
    }

    @Test
    public void continueJobIfNotSoberLastTimestampNotSet() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of(createDuffCheckin()));

        task.run();

        verify(notifier).notify(eq(DRUNK), messageCaptor.capture(), eq(Source.SMS), any());
        assertThat(messageCaptor.getValue())
            .isEqualTo(createBacMessage(0.029, i18nSupport.getLabel("level_lightheaded"), 1));
        assertThat(task.getLastNotified()).isNotNull();
    }

    @Test
    public void continueJobIfNotSoberLastTimestampSetWithinPush() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of(createDuffCheckin()));
        task.setLastNotified(ZonedDateTimes.nowUTC().minusMinutes(25));

        task.run();

        verify(notifier, never()).notify(any(), any(), any(), any());
    }

    @Test
    public void continueJobIfNotSoberLastTimestampSetOutsidePush() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of(createDuffCheckin()));
        final ZonedDateTime past = ZonedDateTimes.nowUTC().minusMinutes(35);
        task.setLastNotified(past);

        task.run();

        verify(notifier).notify(eq(DRUNK), messageCaptor.capture(), eq(Source.SMS), any());
        assertThat(messageCaptor.getValue())
            .isEqualTo(createBacMessage(0.029, i18nSupport.getLabel("level_lightheaded"), 1));
        assertThat(task.getLastNotified()).isGreaterThan(past);
    }

    @Test
    public void addBacCalculation() {
        when(checkinProvider.get(any(), any())).thenReturn(ImmutableList.of(createDuffCheckin()));

        task.run();

        verify(dbService).insertBacCalculation(any(BacCalculation.class));
    }

    @Test
    public void cancelJobIfPersonGone() {
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.empty());

        task.run();

        verify(dbService).stopJob(any(), any());
        verify(executorServiceFactory).stop(any(Key.class));
    }
}
