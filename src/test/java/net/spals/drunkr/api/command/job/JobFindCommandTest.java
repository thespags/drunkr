package net.spals.drunkr.api.command.job;

import static javax.ws.rs.core.Response.Status.OK;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;

/**
 * Unit tests for {@link JobFindCommand}.
 *
 * @author spags
 */
public class JobFindCommandTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final JobOptions JOB = new Builder()
        .userId(DRINKER.id())
        .source(Source.SMS)
        .build();
    private JobFindCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new JobFindCommand();

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRINKER)
            .put("job", JOB)
            .build();
    }

    @Test
    public void jobFound() {
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(JOB);
    }
}
