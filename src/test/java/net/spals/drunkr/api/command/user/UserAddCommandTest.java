package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Unit tests to verify the {@link UserAddCommand} createing a {@link Person}.
 *
 * @author jbrock
 */
public class UserAddCommandTest {

    private static final Person EXISTING = Persons.BROCK;
    private static final String EXISTING_PHONE_NUMBER = Persons.BROCKS_NUMBER;
    private static final String EXISTING_MESSENGER_ID = "messengerId";
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private UserAddCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new UserAddCommand(dbService, i18nSupport);
    }

    @Test
    public void dupeUsername() {
        when(dbService.getPerson(EXISTING.userName())).thenReturn(Optional.of(EXISTING));

        final Response response = command.run(
            ImmutableMap.of("user", EXISTING)
        );

        assertThat(response)
            .hasStatus(BAD_REQUEST)
            .hasErrorMessage(i18nSupport.getLabel("command.create.person.existing.username", EXISTING.userName()));
    }

    @Test
    public void dupePhone() {
        when(dbService.getPerson(EXISTING_PHONE_NUMBER)).thenReturn(Optional.of(EXISTING));

        final Response response = command.run(
            ImmutableMap.of("user", EXISTING)
        );

        assertThat(response)
            .hasStatus(BAD_REQUEST)
            .hasErrorMessage(i18nSupport.getLabel("command.create.person.existing"));
    }

    @Test
    public void dupeMessenger() {
        final Person withFacebook = new Person.Builder()
            .mergeFrom(EXISTING)
            .messengerId(EXISTING_MESSENGER_ID)
            .build();
        when(dbService.getPerson(EXISTING_MESSENGER_ID)).thenReturn(Optional.of(withFacebook));

        final Response response = command.run(
            ImmutableMap.of("user", withFacebook)
        );

        assertThat(response)
            .hasStatus(BAD_REQUEST)
            .hasErrorMessage(i18nSupport.getLabel("command.create.person.existing"));
    }

    @Test
    public void createPhone() {
        when(dbService.insertPerson(any())).thenReturn(true);

        final Response response = command.run(
            ImmutableMap.of("user", EXISTING)
        );

        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(EXISTING);
        verify(dbService).insertPerson(any(Person.class));
    }

    @Test
    public void createMessenger() {
        when(dbService.insertPerson(any())).thenReturn(true);

        final Response response = command.run(
            ImmutableMap.of("user", EXISTING, "source", "MESSENGER")
        );

        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(EXISTING);
        verify(dbService).insertPerson(any(Person.class));
    }

    @Test
    public void createFail() {
        when(dbService.insertPerson(any())).thenReturn(false);

        final Response response = command.run(
            ImmutableMap.of("user", EXISTING, "source", "SMS")
        );

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.create.person.fail"));
    }
}
