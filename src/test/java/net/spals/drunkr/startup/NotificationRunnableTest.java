package net.spals.drunkr.startup;

import static org.mockito.Mockito.*;

import static net.spals.drunkr.startup.NotificationRunnable.ID;
import static net.spals.drunkr.startup.NotificationRunnable.TEXT;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.messenger.MessengerClient;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Unit tests for {@link NotificationRunnable}.
 *
 * @author spags
 */
public class NotificationRunnableTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final String DRUNK_PHONE_NUMBER = Persons.SPAGS_NUMBER;
    private static final String MESSENGER_ID = "messengerId";
    private static final Person DRUNK_NO_PHONE_NUMBER = new Person.Builder()
        .mergeFrom(DRUNK)
        .phoneNumber(Optional.empty())
        .build();
    private static final Person DRUNK_WITH_MESSENGER = new Person.Builder()
        .mergeFrom(DRUNK)
        .messengerId(MESSENGER_ID)
        .build();
    private static final Notification NOTIFICATION = new Notification.Builder()
        .userId(DRUNK.id())
        .message("Hello World")
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    private static final Notification PUSHED_NOTIFICATION = new Notification.Builder()
        .mergeFrom(NOTIFICATION)
        .pushed(true)
        .build();
    private static final Notification SMS_NOTIFICATION = new Notification.Builder()
        .mergeFrom(NOTIFICATION)
        .source(Source.SMS)
        .build();
    private static final Notification SMS_PUSHED_NOTIFICATION = new Notification.Builder()
        .mergeFrom(SMS_NOTIFICATION)
        .pushed(true)
        .build();
    private static final Notification MESSENGER_NOTIFICATION = new Notification.Builder()
        .mergeFrom(NOTIFICATION)
        .source(Source.MESSENGER)
        .build();
    private static final Notification MESSENGER_PUSHED_NOTIFICATION = new Notification.Builder()
        .mergeFrom(MESSENGER_NOTIFICATION)
        .pushed(true)
        .build();
    private static final MessengerResponse MESSAGE = new MessengerResponse.Builder()
        .putRecipient(ID, MESSENGER_ID)
        .putMessage(TEXT, NOTIFICATION.message())
        .messagingType(MessengerResponse.MessageType.UPDATE)
        .build();
    private static final Key KEY = new Key.Builder(NotificationRunnable.class)
        .build();
    @Mock
    private DatabaseService dbService;
    @Mock
    private ExecutorServiceFactory executorServiceFactory;
    @Mock
    private MessengerClient messengerClient;
    @Mock
    private TwilioClient twilioClient;
    @Mock
    private ScheduledExecutorService executor;
    private NotificationRunnable runnable;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        runnable = new NotificationRunnable(
            dbService,
            executorServiceFactory,
            messengerClient,
            twilioClient
        );
        when(executorServiceFactory.createSingleThreadScheduledExecutor(KEY)).thenReturn(executor);
    }

    @Test
    public void noNotifications() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of());

        runnable.run();

        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService, never()).updateNotification(any());
    }

    @Test
    public void fromSmsNotification() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(SMS_NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK));

        runnable.run();

        verify(twilioClient).sendMessage(DRUNK_PHONE_NUMBER, NOTIFICATION.message());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService).updateNotification(SMS_PUSHED_NOTIFICATION);
    }

    @Test
    public void fromSmsNotificationNoPhoneNumber() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(SMS_NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK_NO_PHONE_NUMBER));

        runnable.run();

        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService).updateNotification(SMS_PUSHED_NOTIFICATION);
    }

    /**
     * User only has a phone number and sms has no source.
     */
    @Test
    public void noSourceOnlyPhoneNumber() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK));

        runnable.run();

        verify(twilioClient).sendMessage(DRUNK_PHONE_NUMBER, NOTIFICATION.message());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService).updateNotification(PUSHED_NOTIFICATION);
    }

    @Test
    public void fromMessengerNotification() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(MESSENGER_NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK_WITH_MESSENGER));

        runnable.run();

        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient).sendMessage(MESSAGE);
        verify(dbService).updateNotification(MESSENGER_PUSHED_NOTIFICATION);
    }

    @Test
    public void fromMessengerNotificationNoMessenger() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(MESSENGER_NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK));

        runnable.run();

        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService).updateNotification(MESSENGER_PUSHED_NOTIFICATION);
    }

    /**
     * User has a messenger and phone number, but messenger has higher precedence.
     */
    @Test
    public void noSourceMessenger() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.of(DRUNK_WITH_MESSENGER));

        runnable.run();

        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient).sendMessage(MESSAGE);
        verify(dbService).updateNotification(PUSHED_NOTIFICATION);
    }

    @Test
    public void personDoesNotExist() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(NOTIFICATION));
        when(dbService.getPerson(DRUNK.id().toHexString())).thenReturn(Optional.empty());

        runnable.run();

        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService).updateNotification(PUSHED_NOTIFICATION);
    }

    @Test
    public void handleThrowNoUpdate() {
        when(dbService.unpushedNotifications()).thenReturn(ImmutableList.of(NOTIFICATION));
        // This will throw an NPE that we will handle.
        when(dbService.getPerson(any())).thenReturn(null);

        runnable.run();

        verify(dbService).getPerson(any());
        verify(twilioClient, never()).sendMessage(anyString(), anyString());
        verify(messengerClient, never()).sendMessage(any());
        verify(dbService, never()).updateNotification(PUSHED_NOTIFICATION);
    }

    @Test
    public void handleSubmit() {
        runnable.submit();

        verify(executorServiceFactory).createSingleThreadScheduledExecutor(KEY);
        verify(executor).scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
    }
}