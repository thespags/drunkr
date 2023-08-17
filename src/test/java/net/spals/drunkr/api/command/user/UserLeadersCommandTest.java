package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Optional;

import com.google.common.collect.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;

/**
 * Unit tests for {@link UserLeadersCommand}.
 *
 * @author spags
 */
public class UserLeadersCommandTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final Person OTHER_DRUNK = Persons.BROCK;
    private static final JobOptions JOB = new Builder()
        .userId(DRUNK.id())
        .source(Source.SMS)
        .build();
    @Mock
    private DatabaseService dbService;
    private UserLeadersCommand command;
    private ImmutableMap<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new UserLeadersCommand(dbService);

        // Drunk has a follower and is following other drunk.
        when(dbService.getFollowing(DRUNK)).thenReturn(ImmutableSet.of(OTHER_DRUNK));
        when(dbService.getFollowers(DRUNK)).thenReturn(ImmutableSet.of(OTHER_DRUNK));
        // As long as return a job, it will evaluate drinking to true. We don't care about the job details.
        when(dbService.getRunningJob(eq(OTHER_DRUNK), any())).thenReturn(Optional.of(JOB));
        when(dbService.getRunningJob(eq(DRUNK), any())).thenReturn(Optional.of(JOB));

        request = ImmutableMap.of("user", DRUNK);
    }

    private BacCalculation createBac(final Person person, final double bac) {
        return new BacCalculation.Builder()
            .userId(person.id())
            .bac(bac)
            .timestamp(ZonedDateTimes.nowUTC())
            .build();
    }

    @Test
    public void withDrunkMoreDrunk() {
        when(dbService.getBacCalculations(eq(OTHER_DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(OTHER_DRUNK, 0.05)));
        when(dbService.getBacCalculations(eq(DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(DRUNK, 0.08)));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(
                ImmutableList.of(
                    new BacStatus.Builder().user(OTHER_DRUNK).bac(0.05).isDrinking(true).build(),
                    new BacStatus.Builder().user(DRUNK).bac(0.08).isDrinking(true).build()
                )
            );
    }

    @Test
    public void withOtherDrunkMoreDrunk() {
        when(dbService.getBacCalculations(eq(OTHER_DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(OTHER_DRUNK, 0.08)));
        when(dbService.getBacCalculations(eq(DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(DRUNK, 0.05)));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasListOfEntities(
                new BacStatus.Builder().user(DRUNK).bac(0.05).isDrinking(true).build(),
                new BacStatus.Builder().user(OTHER_DRUNK).bac(0.08).isDrinking(true).build()
            );
    }

    @Test
    public void noCalculations() {
        when(dbService.getBacCalculations(eq(OTHER_DRUNK), any(), any()))
            .thenReturn(ImmutableList.of());
        when(dbService.getBacCalculations(eq(DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(DRUNK, 0.05)));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasListOfEntities(
                new BacStatus.Builder().user(OTHER_DRUNK).bac(0.00).isDrinking(true).build(),
                new BacStatus.Builder().user(DRUNK).bac(0.05).isDrinking(true).build()
            );
    }

    @Test
    public void multipleCalculations() {
        when(dbService.getBacCalculations(eq(OTHER_DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(DRUNK, 0.05), createBac(DRUNK, 0.08)));
        when(dbService.getBacCalculations(eq(DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(DRUNK, 0.05)));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasListOfEntities(
                new BacStatus.Builder().user(DRUNK).bac(0.05).isDrinking(true).build(),
                new BacStatus.Builder().user(OTHER_DRUNK).bac(0.08).isDrinking(true).build()
            );
    }

    @Test
    public void notDrinking() {
        when(dbService.getRunningJob(eq(OTHER_DRUNK), any())).thenReturn(Optional.empty());
        when(dbService.getBacCalculations(eq(OTHER_DRUNK), any(), any()))
            .thenReturn(ImmutableList.of());
        when(dbService.getBacCalculations(eq(DRUNK), any(), any()))
            .thenReturn(ImmutableList.of(createBac(DRUNK, 0.05)));

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasListOfEntities(
                new BacStatus.Builder().user(OTHER_DRUNK).bac(0.00).isDrinking(false).build(),
                new BacStatus.Builder().user(DRUNK).bac(0.05).isDrinking(true).build()
            );
    }
}