package net.spals.drunkr.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.NotificationSubject.assertThat;

import java.time.ZonedDateTime;

import com.google.common.collect.ImmutableSet;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;

public class FollowersNotifierTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final Person FOLLOWER = Persons.BROCK;
    private static final String MESSAGE = "Hello World!!!";
    private static final ZonedDateTime NOW = ZonedDateTimes.nowUTC();
    @Mock
    private DatabaseService dbService;
    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;
    private FollowersNotifier notifier;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        notifier = new FollowersNotifier(dbService);
    }

    @Test
    public void withFollower() {
        when(dbService.getFollowers(DRUNK)).thenReturn(ImmutableSet.of(FOLLOWER));

        notifier.notify(DRUNK, MESSAGE, Source.SMS, NOW);

        verify(dbService, times(2)).insertNotification(notificationCaptor.capture());

        assertThat(notificationCaptor.getValue())
            .hasUserId(FOLLOWER.id())
            .hasSourceUserId(DRUNK.id())
            .hasSource(Source.SMS)
            .hasMessage(MESSAGE)
            .isNotPushed();
    }

    @Test
    public void withNoFollowers() {
        when(dbService.getFollowers(DRUNK)).thenReturn(ImmutableSet.of());

        notifier.notify(DRUNK, MESSAGE, Source.SMS, NOW);

        verify(dbService).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
            .hasUserId(DRUNK.id())
            .hasEmptySourceUserId()
            .hasSource(Source.SMS)
            .hasMessage(MESSAGE)
            .isNotPushed();
    }
}