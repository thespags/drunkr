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
 * Tests for {@link FollowingListCommand} that we can list who we are following.
 *
 * @author spags
 */
public class FollowingListCommandTest {

    private static final Person FOLLOWER = Persons.SPAGS;
    private static final Person USER = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private FollowingListCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new FollowingListCommand(dbService);
    }

    @Test
    public void listFollowees() {
        when(dbService.getFollowing(FOLLOWER)).thenReturn(ImmutableSet.of(USER));
        final Map<String, Object> request = ImmutableMap.<String, Object>builder()
            .put("user", FOLLOWER)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(ImmutableSet.of(USER));
    }
}
