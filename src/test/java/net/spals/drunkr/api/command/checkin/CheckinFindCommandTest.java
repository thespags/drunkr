package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.OK;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.*;

/**
 * Unit tests for {@link CheckinFindCommand}.
 *
 * @author spags
 */
public class CheckinFindCommandTest {

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
    private CheckinFindCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new CheckinFindCommand();

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("checkin", CHECKIN)
            .build();
    }

    @Test
    public void checkinFound() {
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(CHECKIN);
    }
}
