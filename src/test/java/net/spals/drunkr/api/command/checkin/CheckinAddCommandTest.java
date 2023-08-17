package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

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
 * Unit tests to verify the {@link CheckinAddCommand} used to add {@link Checkin} for requesting {@link Person}.
 *
 * @author jbrock
 */
public class CheckinAddCommandTest {

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
    private CheckinAddCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new CheckinAddCommand(dbService, i18nSupport);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("checkin", CHECKIN)
            .build();
    }

    @Test
    public void checkinSuccess() {
        when(dbService.insertCheckin(any())).thenReturn(true);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(CREATED)
            .hasEntity(CHECKIN);
        verify(dbService).insertCheckin(any());
    }

    @Test
    public void checkinFail() {
        when(dbService.insertCheckin(any())).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.checkin.fail", CHECKIN.name()));
        verify(dbService).insertCheckin(any());
    }
}
