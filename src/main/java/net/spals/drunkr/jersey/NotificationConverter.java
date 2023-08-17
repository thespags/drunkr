package net.spals.drunkr.jersey;

import javax.ws.rs.*;
import javax.ws.rs.ext.ParamConverter;
import java.util.Optional;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.Notification;

/**
 * Ability to convert an id from a {@link PathParam}, {@link QueryParam}, etc to a {@link Notification} seamlessly in a resource.
 * Throws a {@link NotFoundException} if the id is invalid.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ParamConverter.class, key = "net.spals.drunkr.model.Notification")
class NotificationConverter implements ParamConverter<Notification> {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    NotificationConverter(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Notification fromString(final String notificationId) {
        final Optional<Notification> notification = dbService.getNotification(notificationId);
        return notification.orElseThrow(() -> new NotFoundException(i18nSupport.getLabel(
            "invalid.notification",
            notificationId
        )));
    }

    @Override
    public String toString(final Notification value) {
        return value.id().toHexString();
    }
}
