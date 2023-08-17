package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;
import static net.spals.drunkr.serialization.ObjectSerializers.createObjectSerializer;

import javax.ws.rs.core.Response;
import java.util.Map;

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
 * Unit tests for {@link UserUpdateCommand}.
 *
 * @author spags
 */
public class UserUpdateCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private UserUpdateCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new UserUpdateCommand(dbService, i18nSupport, createObjectSerializer());

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("payload", ImmutableMap.of("weight", 160))
            .build();
    }

    @Test
    public void userUpdatedSuccess() {
        final Person updatedDrinker = new Person.Builder()
            .mergeFrom(DRINKER)
            .weight(160)
            .build();
        when(dbService.updatePerson(updatedDrinker)).thenReturn(true);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(updatedDrinker);
    }

    @Test
    public void checkinUpdatedFail() {
        final Person updatedDrinker = new Person.Builder()
            .mergeFrom(DRINKER)
            .weight(160)
            .build();
        when(dbService.updatePerson(updatedDrinker)).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.update.user.fail", updatedDrinker.id()));
    }
}