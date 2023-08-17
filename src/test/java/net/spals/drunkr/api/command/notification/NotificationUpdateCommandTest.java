package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;
import static net.spals.drunkr.serialization.ObjectSerializers.createObjectSerializer;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.Notification.Builder;

/**
 * Unit tests for {@link NotificationUpdateCommand}.
 *
 * @author spags
 */
public class NotificationUpdateCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final Notification NOTIFICATION = new Builder()
        .userId(DRINKER.id())
        .message("Hello World")
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private NotificationUpdateCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new NotificationUpdateCommand(dbService, i18nSupport, createObjectSerializer());
        when(dbService.getNotification(NOTIFICATION.id().toHexString())).thenReturn(Optional.of(NOTIFICATION));

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("notification", NOTIFICATION)
            .put("payload", ImmutableMap.of("read", true))
            .build();
    }

    @Test
    public void notificationUpdatedSuccess() {
        final Notification updatedNotification = new Notification.Builder()
            .mergeFrom(NOTIFICATION)
            .read(true)
            .build();
        when(dbService.updateNotification(updatedNotification)).thenReturn(true);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(updatedNotification);
    }

    @Test
    public void notificationUpdatedFail() {
        when(dbService.updateNotification(NOTIFICATION)).thenReturn(false);
        final Notification updatedNotification = new Notification.Builder()
            .mergeFrom(NOTIFICATION)
            .read(true)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.notification.update.fail", updatedNotification.id()));
    }
}
