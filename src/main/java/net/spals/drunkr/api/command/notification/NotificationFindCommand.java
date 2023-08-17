package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.Notification;

/**
 * Find a specific {@link Notification} given an id.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "NOTIFICATION_FIND", keyType = CommandType.class)
class NotificationFindCommand implements ApiCommand {

    @Inject
    NotificationFindCommand() {
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Notification notification = (Notification) request.get("notification");

        return Response.status(OK)
            .entity(notification)
            .build();
    }
}