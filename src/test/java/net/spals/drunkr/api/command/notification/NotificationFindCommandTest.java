package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.OK;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.Notification.Builder;

/**
 * Unit tests for {@link NotificationFindCommand}.
 *
 * @author spags
 */
public class NotificationFindCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final Notification NOTIFICATION = new Builder()
        .userId(DRINKER.id())
        .message("Hello World")
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    private NotificationFindCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new NotificationFindCommand();

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("notification", NOTIFICATION)
            .build();
    }

    @Test
    public void notificationFound() {
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(NOTIFICATION);
    }
}
