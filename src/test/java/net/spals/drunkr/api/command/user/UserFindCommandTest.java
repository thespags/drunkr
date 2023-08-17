package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.OK;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Tests for {@link UserFindCommand}. User lookups are performed by Jersey's {@link ParamConverter}
 * so this test has not direct negative test case.
 *
 * @author spags
 */
public class UserFindCommandTest {

    private static final Person FINDEE = Persons.BROCK;
    private UserFindCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new UserFindCommand();

        request = ImmutableMap.of("user", FINDEE);
    }

    @Test
    public void foundUser() {
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(FINDEE);
    }
}