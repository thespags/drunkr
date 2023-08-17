package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.Notification.Builder;

/**
 * Unit tests for {@link NotificationFindAllCommand}.
 *
 * @author spags
 */
public class NotificationFindAllCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final ZonedDateTime FROM = ZonedDateTimes.nowUTC();
    private static final ZonedDateTime TO = FROM.plusMinutes(8);
    private static final Notification NOTIFICATION = new Builder()
        .userId(DRINKER.id())
        .message("Hello World")
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    @Mock
    private DatabaseService dbService;
    private NotificationFindAllCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new NotificationFindAllCommand(dbService);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("from", Optional.of(FROM))
            .put("to", Optional.of(TO))
            .build();
    }

    @Test
    public void findAll() {
        final List<Notification> notifications = ImmutableList.of(NOTIFICATION);
        when(dbService.getNotifications(DRINKER, Optional.of(FROM), Optional.of(TO))).thenReturn(notifications);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(notifications);
    }
}
