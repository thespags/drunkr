package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.serialization.ObjectSerializer;

/**
 * Update an existing {@link Notification} for the requesting {@link Person}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "NOTIFICATION_UPDATE", keyType = CommandType.class)
class NotificationUpdateCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;
    private final ObjectSerializer serializer;

    @Inject
    NotificationUpdateCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport,
        final ObjectSerializer serializer
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
        this.serializer = serializer;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Notification notification = (Notification) request.get("notification");

        //noinspection unchecked
        final Map<String, Object> payload = (Map<String, Object>) request.get("payload");
        final Notification updatedNotification = serializer.patch(notification, payload, Notification.class);
        final boolean updated = dbService.updateNotification(updatedNotification);

        if (updated) {
            return Response.status(OK)
                .entity(updatedNotification)
                .build();
        }

        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.notification.update.fail", updatedNotification.id())
        ).asResponseBuilder().build();
    }
}
