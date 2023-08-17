package net.spals.drunkr.api.command;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.api.UntappdResource;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Tests for {@link UntappdResource} when linking Untappd Auth with a user.
 *
 * @author jbrock
 */
public class UntappdAuthCommandTest {

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String UNTAPPD_USER_NAME = "untappdUserName";
    private static final Person USER = Persons.SPAGS;
    private static final UntappdLink LINK = new UntappdLink.Builder()
        .userId(USER.id())
        .untappdName(UNTAPPD_USER_NAME)
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private UntappdAuthCommand resource;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        resource = new UntappdAuthCommand(dbService, i18nSupport);

        when(dbService.getUntappdLink(USER)).thenReturn(Optional.of(LINK));
        request = ImmutableMap.<String, Object>builder()
            .put("untappdUserName", UNTAPPD_USER_NAME)
            .put("accessToken", ACCESS_TOKEN)
            .build();
    }

    @Test
    public void linkAuth() {
        when(dbService.getUntappdLink(anyString())).thenReturn(Optional.of(LINK));
        when(dbService.updateLinkAccessToken(any(), any())).thenReturn(true);

        final Response response = resource.run(request);

        verify(dbService).updateLinkAccessToken(any(), eq(ACCESS_TOKEN));
        assertThat(response)
            .hasStatus(OK)
            .hasEntity(LINK);
    }

    @Test
    public void updateFail() {
        when(dbService.getUntappdLink(anyString())).thenReturn(Optional.of(LINK));
        when(dbService.updateLinkAccessToken(any(), any())).thenReturn(false);

        final Response response = resource.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.link.untappd.auth.fail", UNTAPPD_USER_NAME));
    }

    @Test
    public void linkNotFound() {
        when(dbService.getUntappdLink(anyString())).thenReturn(Optional.empty());

        final Response response = resource.run(request);

        verify(dbService, never()).updateLinkAccessToken(any(), any());
        assertThat(response)
            .hasStatus(NOT_FOUND)
            .hasErrorMessage(i18nSupport.getLabel("command.link.untappd.auth.missing", UNTAPPD_USER_NAME));
    }
}

