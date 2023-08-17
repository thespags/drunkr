package net.spals.drunkr.startup;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.messenger.MessengerClient;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Task for pushing notifications to users.
 * <p>
 * As {@link Source#MOBILE} is not implemented we will attempt to send to SMS and Facebook messenger instead if available.
 *
 * @author spags
 */
@AutoBindSingleton
class NotificationRunnable implements Runnable {

    @VisibleForTesting
    static final String ID = "id";
    @VisibleForTesting
    static final String TEXT = "text";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRunnable.class);
    private final DatabaseService dbService;
    private final ExecutorServiceFactory executorServiceFactory;
    private final MessengerClient messengerClient;
    private final TwilioClient twilioClient;

    @Inject
    NotificationRunnable(
        final DatabaseService dbService,
        final ExecutorServiceFactory executorServiceFactory,
        final MessengerClient messengerClient,
        final TwilioClient twilioClient
    ) {
        this.dbService = dbService;
        this.executorServiceFactory = executorServiceFactory;
        this.messengerClient = messengerClient;
        this.twilioClient = twilioClient;
    }

    @PostConstruct
    void submit() {
        LOGGER.info("starting notification process");
        final Key key = new Key.Builder(NotificationRunnable.class)
            .build();
        final ScheduledExecutorService executor = executorServiceFactory.createSingleThreadScheduledExecutor(key);
        executor.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        final List<Notification> notifications = dbService.unpushedNotifications();
        if (!notifications.isEmpty()) {
            LOGGER.info("processing notifications: number=" + notifications.size());
        }

        for (final Notification notification : notifications) {
            final Optional<Person> queriedPerson = dbService.getPerson(notification.userId().toHexString());

            try {
                // If the person exist, first try to send to messenger, then sms, then give up.
                if (queriedPerson.isPresent()) {
                    LOGGER.info("sending message=" + notification.message());
                    final Person person = queriedPerson.get();

                    if (notification.source().map(x -> x == Source.MESSENGER || x == Source.MOBILE).orElse(true)
                        && person.messengerId().isPresent()
                    ) {
                        final String messengerId = person.messengerId().get();
                        final MessengerResponse messageToSend = new MessengerResponse.Builder()
                            .putRecipient(ID, messengerId)
                            .putMessage(TEXT, notification.message())
                            .messagingType(MessengerResponse.MessageType.UPDATE)
                            .build();
                        final int result = messengerClient.sendMessage(messageToSend);
                        LOGGER.info("sent messengerId=" + messengerId + " statusCode=" + result);
                    } else if (notification.source().map(x -> x == Source.SMS || x == Source.MOBILE).orElse(true)
                        && person.phoneNumber().isPresent()
                    ) {
                        final String phoneNumber = person.phoneNumber().get();
                        twilioClient.sendMessage(phoneNumber, notification.message());
                    } else if (notification.source().map(x -> x == Source.MOBILE).orElse(false)) {
                        LOGGER.info("sent to mobile, push to mobile not yet implemented");
                    } else {
                        LOGGER.info("no where to push the notification");
                    }
                }
                markNotificationAsPushed(notification);
            } catch (final Throwable x) {
                LOGGER.info("unable to push notification: " + notification, x);
            }
        }
    }

    private void markNotificationAsPushed(final Notification notification) {
        final Notification pushedNotification = new Notification.Builder()
            .mergeFrom(notification)
            .pushed(true)
            .build();
        dbService.updateNotification(pushedNotification);
    }
}
