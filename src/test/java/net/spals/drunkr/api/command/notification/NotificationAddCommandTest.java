package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

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
 * Unit tests for {@link NotificationAddCommand}.
 *
 * @author spags
 */
public class NotificationAddCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final Notification NOTIFICATION = new Builder()
        .userId(DRINKER.id())
        .message("Hello World")
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private NotificationAddCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new NotificationAddCommand(dbService, i18nSupport);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("notification", NOTIFICATION)
            .build();
    }

    @Test
    public void notificationSuccess() {
        when(dbService.insertNotification(any())).thenReturn(true);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(NOTIFICATION);
        verify(dbService).insertNotification(any());
    }

    @Test
    public void notificationFail() {
        when(dbService.insertNotification(any())).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.notification.fail", NOTIFICATION.id()));
        verify(dbService).insertNotification(any());
    }
}