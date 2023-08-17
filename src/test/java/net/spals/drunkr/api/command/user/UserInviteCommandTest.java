package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Unit tests for {@link UserInviteCommand} ensuring correct logic is executed.
 *
 * @author jbrock
 */
public class UserInviteCommandTest {

    private static final Person INVITER = Persons.SPAGS;
    private static final String INVITER_PHONE_NUMBER = Persons.SPAGS_NUMBER;
    private static final Person INVITEE = Persons.BROCK;
    private static final String INVITEE_PHONE_NUMBER = Persons.BROCKS_NUMBER;
    @Mock
    private DatabaseService dbService;
    @Mock
    private TwilioClient twilioClient;
    private I18nSupport i18nSupport;
    private UserInviteCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new UserInviteCommand(dbService, i18nSupport, twilioClient);
        when(dbService.getPerson(INVITER_PHONE_NUMBER)).thenReturn(Optional.of(INVITER));
    }

    /**
     * Creates an unaccepted {@link Invite} from {@link #INVITER} to the provided invitee which may or may not be valid.
     *
     * @param invitee a phone number which may or may not be invalid
     * @return an unccepted {@link Invite}
     */
    private Invite createUnacceptedInvite(final String invitee) {
        return new Invite.Builder()
            .userId(INVITER.id())
            .phoneNumber(invitee)
            .build();
    }

    @Test
    public void invite() {
        when(dbService.getPerson(INVITEE_PHONE_NUMBER)).thenReturn(Optional.empty());
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", INVITER)
            .put("invite", createUnacceptedInvite(INVITEE_PHONE_NUMBER))
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(INVITEE_PHONE_NUMBER);
        final String help = i18nSupport.getLabel("command.create.help");
        final String inviteMessage = i18nSupport.getLabel("invite.person", INVITER.userName(), help);
        verify(twilioClient).sendMessage(eq(INVITEE_PHONE_NUMBER), eq(inviteMessage));
    }

    @Test
    public void inviteSelf() {
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", INVITER)
            .put("invite", createUnacceptedInvite(INVITER_PHONE_NUMBER))
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("invite.person.same"));
    }

    @Test
    public void inviteExistingPersonStandardPhoneNumber() {
        when(dbService.getPerson(INVITEE_PHONE_NUMBER)).thenReturn(Optional.of(INVITEE));
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", INVITER)
            .put("invite", createUnacceptedInvite(INVITEE_PHONE_NUMBER))
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.find.person", INVITEE.userName()));
    }

    @Test
    public void inviteExistingPersonNonStandardPhoneNumber() {
        when(dbService.getPerson(INVITEE_PHONE_NUMBER)).thenReturn(Optional.of(INVITEE));
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", INVITER)
            .put("invite", createUnacceptedInvite("(575)430-4788"))
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CONFLICT)
            .hasErrorMessage(i18nSupport.getLabel("command.find.person", INVITEE.userName()));
    }

    @Test
    public void invalidPhoneNumber() {
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", INVITER)
            .put("invite", createUnacceptedInvite("JohnBrock"))
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(NOT_FOUND)
            .hasErrorMessage(i18nSupport.getLabel("command.invite.invalid.phone.number"));
    }

    @Test
    public void emptyPhoneNumber() {
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", INVITER)
            .put("invite", createUnacceptedInvite(""))
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(NOT_FOUND)
            .hasErrorMessage(i18nSupport.getLabel("command.invite.invalid.phone.number"));
    }
}
