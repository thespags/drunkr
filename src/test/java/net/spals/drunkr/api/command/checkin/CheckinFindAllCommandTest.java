package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;

/**
 * Unit tests for {@link CheckinFindAllCommand}.
 *
 * @author spags
 */
public class CheckinFindAllCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final ZonedDateTime FROM = ZonedDateTimes.nowUTC();
    private static final ZonedDateTime TO = FROM.plusMinutes(8);
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
    private CheckinFindAllCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new CheckinFindAllCommand(dbService);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("from", Optional.of(FROM))
            .put("to", Optional.of(TO))
            .build();
    }

    @Test
    public void findAll() {
        final List<Checkin> checkins = ImmutableList.of(CHECKIN);
        when(dbService.getCheckins(DRINKER, Optional.of(FROM), Optional.of(TO))).thenReturn(checkins);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(checkins);
    }
}
