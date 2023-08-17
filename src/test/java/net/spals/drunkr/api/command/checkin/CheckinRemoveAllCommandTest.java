package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.common.collect.ImmutableList;
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
 * Unit tests for {@link CheckinRemoveAllCommand}.
 *
 * @author spags
 */
public class CheckinRemoveAllCommandTest {

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
    private CheckinRemoveAllCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new CheckinRemoveAllCommand(dbService, i18nSupport);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .build();
    }

    @Test
    public void removeAllSuccess() {
        final List<Checkin> checkins = ImmutableList.of(CHECKIN);
        when(dbService.removeCheckins(DRINKER)).thenReturn(true);
        when(dbService.getCheckins(DRINKER, Optional.empty(), Optional.empty())).thenReturn(checkins);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(checkins);
    }

    @Test
    public void removeAllFailed() {
        when(dbService.removeCheckins(DRINKER)).thenReturn(false);
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.checkin.remove.all.fail"));
    }
}