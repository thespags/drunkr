package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Notification;
import net.spals.drunkr.model.Person;

/**
 * Reads all {@link Notification}s associated to the given user.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "NOTIFICATION_READ_ALL", keyType = CommandType.class)
public class NotificationReadAllCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    NotificationReadAllCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        final boolean read = dbService.markAllReadNotifications(person);

        return Response.status(OK)
            .entity(ImmutableMap.of("read", read))
            .build();
    }
}
