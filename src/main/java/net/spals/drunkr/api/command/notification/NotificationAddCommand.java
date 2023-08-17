package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Add a new {@link Notification} for requesting {@link Person}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "NOTIFICATION_ADD", keyType = CommandType.class)
class NotificationAddCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    NotificationAddCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Notification notification = (Notification) request.get("notification");
        final boolean added = dbService.insertNotification(notification);

        if (added) {
            return Response.status(CREATED)
                .entity(notification)
                .build();
        }

        return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.notification.fail", notification.id()))
            .asResponseBuilder()
            .build();
    }
}
