package net.spals.drunkr.service;

import java.time.ZonedDateTime;
import java.util.Set;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;

/**
 * Notifiers your following with the provided message.
 */
@AutoBindSingleton
public class FollowersNotifier {

    private final DatabaseService dbService;

    @Inject
    FollowersNotifier(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    void notify(
        final Person person,
        final String message,
        final Source source,
        final ZonedDateTime timestamp
    ) {
        // Notify the user initiated the job.
        final Notification notification = new Notification.Builder()
            .userId(person.id())
            .message(message)
            .source(source)
            .timestamp(timestamp)
            .build();
        dbService.insertNotification(notification);

        // Notify all of the user's followers.
        final Set<Person> followers = dbService.getFollowers(person);
        for (final Person follower : followers) {
            final Notification followerNotification = new Notification.Builder()
                .userId(follower.id())
                .sourceUserId(person.id())
                .source(source)
                .message(message)
                .timestamp(timestamp)
                .build();
            dbService.insertNotification(followerNotification);
        }
    }
}
