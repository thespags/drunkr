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
 * Tests for {@link FollowingAddCommand} that we can add a following.
 *
 * @author spags
 */
public class FollowingAddCommandTest {

    private static final Person FOLLOWER = Persons.SPAGS;
    private static final Person USER = Persons.BROCK;
    private static final String USER_PHONE_NUMBER = Persons.BROCKS_NUMBER;
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;
    private FollowingAddCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new FollowingAddCommand(dbService, i18nSupport);
        when(dbService.getPerson(USER_PHONE_NUMBER)).thenReturn(Optional.of(USER));
    }

    private Map<String, Object> buildRequest(final String targetUserId) {
        return ImmutableMap.<String, Object>builder()
            .put("user", FOLLOWER)
            .put("targetUserId", targetUserId)
            .build();
    }

    private Map<String, Person> expectedSuccessEntity() {
        return ImmutableMap.of(
            "followee", USER,
            "follower", FOLLOWER
        );
    }

    @Test
    public void addFolloweeInvalid() {
        when(dbService.getPerson(USER_PHONE_NUMBER)).thenReturn(Optional.empty());

        final Response response = command.run(buildRequest(USER_PHONE_NUMBER));

        assertThat(response)
            .hasStatus(NOT_FOUND)
            .hasErrorMessage(i18nSupport.getLabel("invalid.user", USER_PHONE_NUMBER));
    }

    @Test
    public void addFolloweeSelf() {
        when(dbService.getPerson(USER_PHONE_NUMBER)).thenReturn(Optional.of(FOLLOWER));

        final Response response = command.run(buildRequest(USER_PHONE_NUMBER));

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.follow.add.self"));
    }

    @Test
    public void addFolloweeAlreadyExists() {
        when(dbService.getFollowing(FOLLOWER)).thenReturn(ImmutableSet.of(USER));

        final Response response = command.run(buildRequest(USER_PHONE_NUMBER));

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.follow.add.exists", USER.userName()));
    }

    @Test
    public void addFolloweeFail() {
        when(dbService.getFollowing(FOLLOWER)).thenReturn(ImmutableSet.of());
        when(dbService.addFollower(FOLLOWER, USER)).thenReturn(false);

        final Response response = command.run(buildRequest(USER_PHONE_NUMBER));

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.follow.add.fail", USER.userName()));
    }

    @Test
    public void addFolloweeSuccess() {
        when(dbService.getFollowing(FOLLOWER)).thenReturn(ImmutableSet.of());
        when(dbService.addFollower(USER, FOLLOWER)).thenReturn(true);

        final Response response = command.run(buildRequest(USER_PHONE_NUMBER));

        verify(dbService).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
            .hasSourceUserId(FOLLOWER.id())
            .hasUserId(USER.id())
            .hasMessage(i18nSupport.getLabel("command.follow.add.message", FOLLOWER.userName()));
        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(expectedSuccessEntity());
    }

    /**
     * You can enter in a phone number multiple ways. We expect the command to handle
     * (412)251-3259, 412-251-3259, etc as the standard +14122513259.
     */
    @Test
    public void addFolloweeSuccessNonStandardPhoneNumber() {
        when(dbService.getFollowing(FOLLOWER)).thenReturn(ImmutableSet.of());
        when(dbService.addFollower(USER, FOLLOWER)).thenReturn(true);

        final Response response = command.run(buildRequest("575-430-4788"));

        verify(dbService).insertNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue())
            .hasSourceUserId(FOLLOWER.id())
            .hasUserId(USER.id())
            .hasMessage(i18nSupport.getLabel("command.follow.add.message", FOLLOWER.userName()));
        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(expectedSuccessEntity());
    }
}
