package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;

/**
 * Unit tests for {@link JobCheckCommand}.
 *
 * @author spags
 */
public class JobCheckCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final JobOptions JOB = new Builder()
        .userId(DRINKER.id())
        .source(Source.SMS)
        .build();
    @Mock
    private DatabaseService dbService;
    private JobCheckCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new JobCheckCommand(dbService);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .build();
    }

    @Test
    public void hasRunningJob() {
        when(dbService.getRunningJob(eq(DRINKER), any())).thenReturn(Optional.of(JOB));

        final Response response = command.run(request);

        assertThat(response).hasStatus(OK).hasEntity(Optional.of(JOB));
    }

    @Test
    public void hasNoRunningJob() {
        when(dbService.getRunningJob(eq(DRINKER), any())).thenReturn(Optional.empty());

        final Response response = command.run(request);

        assertThat(response).hasStatus(OK).hasEntity(Optional.empty());
    }
}