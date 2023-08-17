package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.NotificationSubject.assertThat;
import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Tests for {@link FollowerInviteCommand} that we can invite users to follow us.
 *
 * @author spags
 */
public class FollowerInviteCommandTest {

    private static final Person USER = Persons.SPAGS;
    private static final Person FOLLOWER = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private FollowerInviteCommand command;
    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new FollowerInviteCommand(dbService, i18nSupport);
        when(dbService.getPerson(FOLLOWER.id().toHexString())).thenReturn(Optional.of(FOLLOWER));

        request = ImmutableMap.<String, Object>builder()
            .put("user", USER)
            .put("targetUserId", FOLLOWER.id().toHexString())
            .build();
    }

    @Test
    public void inviteFollowerInvalid() {
        when(dbService.getPerson(FOLLOWER.id().toHexString())).thenReturn(Optional.empty());

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(NOT_FOUND)
            .hasErrorMessage(i18nSupport.getLabel("invalid.user", FOLLOWER.id().toHexString()));
    }

    @Test
    public void inviteFollowerSelf() {
        when(dbService.getPerson(FOLLOWER.id().toHexString())).thenReturn(Optional.of(USER));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.follower.invite.self"));
    }

    @Test
    public void inviteFollowerExists() {
        when(dbService.getFollowers(USER)).thenReturn(ImmutableSet.of(FOLLOWER));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.follower.invite.exists", FOLLOWER.userName()));
    }

    @Test
    public void inviteFollowerSuccess() {
        when(dbService.getFollowers(USER)).thenReturn(ImmutableSet.of());
        when(dbService.getPerson(FOLLOWER.id().toHexString())).thenReturn(Optional.of(FOLLOWER));
        when(dbService.addFollower(USER, FOLLOWER)).thenReturn(true);

        final Response response = command.run(request);

        verify(dbService).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
            .hasSourceUserId(USER.id())
            .hasUserId(FOLLOWER.id())
            .hasMessage(i18nSupport.getLabel("command.follower.invite.message", USER.userName()));
        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(FOLLOWER);
    }
}