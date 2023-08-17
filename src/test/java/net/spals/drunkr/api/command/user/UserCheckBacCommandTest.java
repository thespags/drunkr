package net.spals.drunkr.api.command.user;

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
 * Unit tests for {@link UserCheckBacCommand} verifying command to retrieve latest BAC for {@link Person}.
 *
 * @author jbrock
 */
public class UserCheckBacCommandTest {

    private static final Person DRINKER = Persons.SPAGS;
    private static final ZonedDateTime FROM = ZonedDateTimes.nowUTC();
    private static final ZonedDateTime TO = FROM.plusMinutes(8);
    @Mock
    private DatabaseService dbService;
    private UserCheckBacCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new UserCheckBacCommand(dbService);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("from", Optional.of(FROM))
            .put("to", Optional.of(TO))
            .build();
    }

    @Test
    public void findAll() {
        final BacCalculation calculation = new BacCalculation.Builder()
            .userId(DRINKER.id())
            .bac(.2)
            .timestamp(FROM)
            .build();
        final List<BacCalculation> calculations = ImmutableList.of(calculation);
        when(dbService.getBacCalculations(DRINKER, Optional.of(FROM), Optional.of(TO))).thenReturn(calculations);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(calculations);
    }
}
