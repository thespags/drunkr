package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Tests for {@link UserFindAllCommand}.
 *
 * @author spags
 */
public class UserFindAllCommandTest {

    private static final Person FINDER = Persons.SPAGS;
    private static final Person FINDEE = Persons.BROCK;
    @Mock
    private DatabaseService dbService;
    private UserFindAllCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        command = new UserFindAllCommand(dbService);
        when(dbService.allPersons()).thenReturn(ImmutableList.of(FINDER, FINDEE));
    }

    @Test
    public void findAll() {
        final Response response = command.run(ImmutableMap.of());

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(ImmutableList.of(FINDER, FINDEE));
    }
}
