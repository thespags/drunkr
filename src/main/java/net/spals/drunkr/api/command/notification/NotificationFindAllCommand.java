package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Notification;
import net.spals.drunkr.model.Person;

/**
 * Finds all {@link Notification}s associated to the given user within the specified time frame.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "NOTIFICATION_FIND_ALL", keyType = CommandType.class)
class NotificationFindAllCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    NotificationFindAllCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        //noinspection unchecked
        final Optional<ZonedDateTime> fromTime = (Optional<ZonedDateTime>) request.get("from");
        //noinspection unchecked
        final Optional<ZonedDateTime> toTime = (Optional<ZonedDateTime>) request.get("to");

        // For now we return all notifications.
        final List<Notification> notifications = dbService.getNotifications(person, fromTime, toTime);

        return Response.status(OK)
            .entity(notifications)
            .build();
    }
}