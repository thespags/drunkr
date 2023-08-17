package net.spals.drunkr.api.command.notification;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Unit tests for {@link NotificationReadAllCommand}
 *
 * @author spags
 */
public class NotificationReadAllCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private NotificationReadAllCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new NotificationReadAllCommand(dbService);
        when(dbService.markAllReadNotifications(DRINKER)).thenReturn(true);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .build();
    }

    @Test
    public void readAll() {
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(ImmutableMap.of("read", true));
    }
}