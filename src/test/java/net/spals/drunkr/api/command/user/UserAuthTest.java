package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;
import static net.spals.drunkr.model.LinkType.AUTH_USER;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Unit tests for {@link UserAuthCommand}.
 *
 * @author spags
 */
public class UserAuthTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final String DRUNKS_NUMBER = Persons.SPAGS_NUMBER;
    private static final String CODE = "012345";
    private static final LinkCode LINK_CODE = new LinkCode.Builder()
        .userId(DRUNK.id())
        .link(DRUNKS_NUMBER)
        .code(CODE)
        .type(AUTH_USER)
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    @Mock
    private DatabaseService dbService;
    @Mock
    private KeyStore keyStore;
    private I18nSupport i18nSupport;
    private UserAuthCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new UserAuthCommand(dbService, keyStore, i18nSupport);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRUNK)
            .put("code", CODE)
            .build();
        when(dbService.getLinkCode(DRUNK, AUTH_USER)).thenReturn(Optional.of(LINK_CODE));
        when(dbService.updatePerson(any())).thenReturn(true);
        when(keyStore.decrypt(any())).thenReturn(CODE);
    }

    @Test
    public void noAuthCode() {
        when(dbService.getLinkCode(DRUNK, AUTH_USER)).thenReturn(Optional.empty());

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.link.phone.auth.not.found"));
    }

    @Test
    public void invalidAuthCode() {
        request = ImmutableMap.<String, Object>builder()
            .put("user", DRUNK)
            .put("code", "123456")
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(ImmutableMap.of("valid", false));
    }

    @Test
    public void validAuthCode() {
        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(OK)
            .hasEntity(ImmutableMap.of("valid", true));
    }
}
