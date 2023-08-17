package net.spals.drunkr.api.command.link;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.Mockito.*;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Tests for {@link LinkUntappdCommand} when linking an Untappd account with a user.
 *
 * @author jbrock
 */
public class LinkUntappdCommandTest {

    private static final String UNTAPPD_USER_NAME = "untappdUserName";
    private static final Person USER = Persons.SPAGS;
    @Mock
    private DatabaseService dbService;
    @Captor
    private ArgumentCaptor<UntappdLink> captor;
    private I18nSupport i18nSupport;
    private LinkUntappdCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new LinkUntappdCommand(dbService, i18nSupport);

        request = ImmutableMap.<String, Object>builder()
            .put("user", USER)
            .put("userName", UNTAPPD_USER_NAME)
            .build();
    }

    @Test
    public void linkUntappd() {
        when(dbService.insertUntappdLink(any())).thenReturn(true);

        final Response response = command.run(request);

        verify(dbService).insertUntappdLink(captor.capture());
        final UntappdLink link = new UntappdLink.Builder()
            .id(captor.getValue().id())
            .userId(USER.id())
            .untappdName(UNTAPPD_USER_NAME)
            .build();
        assertThat(response)
            .hasStatus(OK)
            .hasEntity(link);
    }

    @Test
    public void updateFail() {
        when(dbService.insertUntappdLink(any())).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.link.untappd.fail", USER.userName(), UNTAPPD_USER_NAME));
    }
}

