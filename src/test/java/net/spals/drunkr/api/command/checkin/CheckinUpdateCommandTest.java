package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;
import static net.spals.drunkr.serialization.ObjectSerializers.createObjectSerializer;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

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

/**
 * Unit tests for {@link CheckinFindCommand}.
 *
 * @author spags
 */
public class CheckinUpdateCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final Checkin CHECKIN = new Checkin.Builder()
        .userId(DRINKER.id())
        .name("Duff")
        .producer("Duff Brewery")
        .timestamp(ZonedDateTimes.nowUTC())
        .style(Style.DRAFT)
        .size(Style.DRAFT.getServingSize())
        .abv(.05)
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private CheckinUpdateCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new CheckinUpdateCommand(dbService, i18nSupport, createObjectSerializer());
        when(dbService.getCheckin(CHECKIN.id().toHexString())).thenReturn(Optional.of(CHECKIN));

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("checkin", CHECKIN)
            .put("payload", ImmutableMap.of("size", 24))
            .build();
    }

    @Test
    public void checkinUpdatedSuccess() {
        final Checkin updatedCheckin = new Checkin.Builder()
            .mergeFrom(CHECKIN)
            .size(24)
            .build();
        when(dbService.updateCheckin(updatedCheckin)).thenReturn(true);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(updatedCheckin);
    }

    @Test
    public void checkinUpdatedFail() {
        when(dbService.updateCheckin(CHECKIN)).thenReturn(false);
        final Checkin updatedCheckin = new Checkin.Builder()
            .mergeFrom(CHECKIN)
            .size(24)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.checkin.update.fail", updatedCheckin.id()));
    }
}
