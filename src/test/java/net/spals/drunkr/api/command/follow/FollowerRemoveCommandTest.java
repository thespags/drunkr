package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.NotificationSubject;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Tests for {@link FollowerRemoveCommand} that we remove users following us.
 *
 * @author spags
 */
public class FollowerRemoveCommandTest {

    private static final Person USER = Persons.SPAGS;
    private static final Person FOLLOWER = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private FollowerRemoveCommand command;
    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new FollowerRemoveCommand(dbService, i18nSupport);
        request = ImmutableMap.<String, Object>builder()
            .put("user", USER)
            .put("targetUser", FOLLOWER)
            .build();
    }

    @Test
    public void removeFollowerDoesNotExists() {
        when(dbService.getFollowers(USER)).thenReturn(ImmutableSet.of());

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(NOT_FOUND)
            .hasErrorMessage(i18nSupport.getLabel("command.follower.remove.not.exists", FOLLOWER.userName()));
    }

    @Test
    public void removeFollowerFail() {
        when(dbService.getFollowers(USER)).thenReturn(ImmutableSet.of(FOLLOWER));
        when(dbService.addFollower(USER, FOLLOWER)).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.follower.remove.fail", FOLLOWER.userName()));
    }

    @Test
    public void removeFollowerSuccess() {
        when(dbService.getFollowers(USER)).thenReturn(ImmutableSet.of(FOLLOWER));
        when(dbService.removeFollower(USER, FOLLOWER)).thenReturn(true);

        final Response response = command.run(request);

        verify(dbService).insertNotification(notificationCaptor.capture());
        NotificationSubject.assertThat(notificationCaptor.getValue())
            .hasSourceUserId(USER.id())
            .hasUserId(FOLLOWER.id())
            .hasMessage(i18nSupport.getLabel("command.follower.remove.message", USER.userName()));
        assertThat(response)
            .hasStatus(OK)
            .hasEntity(FOLLOWER);
    }
}
