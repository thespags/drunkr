package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Tests for {@link FollowerListCommand} that we can list who is following us.
 *
 * @author spags
 */
public class FollowerListCommandTest {

    private static final Person USER = Persons.SPAGS;
    private static final Person FOLLOWER = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private FollowerListCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new FollowerListCommand(dbService);
    }

    @Test
    public void listFollowers() {
        when(dbService.getFollowers(USER)).thenReturn(ImmutableSet.of(FOLLOWER));
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", USER)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(ImmutableSet.of(FOLLOWER));
    }
}
