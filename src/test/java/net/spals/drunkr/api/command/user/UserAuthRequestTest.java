package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.truth.Truth;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.common.CodeGenerator;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Unit tests for {@link UserAuthRequestCommand}.
 *
 * @author spags
 */
public class UserAuthRequestTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final String DRUNKS_NUMBER = Persons.SPAGS_NUMBER;
    private static final String SID = "SID";
    private static final String CODE = "012345";
    @Mock
    private CodeGenerator generator;
    @Mock
    private DatabaseService dbService;
    @Mock
    private KeyStore keyStore;
    @Mock
    private TwilioClient twilioClient;
    @Captor
    private ArgumentCaptor<String> captor;
    private I18nSupport i18nSupport;
    private UserAuthRequestCommand command;
    private Map<String, Object> request;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new UserAuthRequestCommand(generator, dbService, keyStore, i18nSupport, twilioClient);

        request = ImmutableMap.<String, Object>builder()
            .put("user", DRUNK)
            .build();
        when(twilioClient.sendMessage(eq(DRUNKS_NUMBER), any())).thenReturn(SID);
        when(dbService.getPerson(DRUNKS_NUMBER)).thenReturn(Optional.empty());
        when(dbService.insertLinkCode(any())).thenReturn(true);
        when(keyStore.encrypt(any())).thenReturn("foo");
        when(generator.generate()).thenReturn(CODE);
    }

    @Test
    public void invalidPhoneNumber() {
        final Person user = new Person.Builder()
            .mergeFrom(DRUNK)
            .phoneNumber(Optional.empty())
            .build();
        request = ImmutableMap.<String, Object>builder()
            .put("user", user)
            .build();

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.link.phone.request.invalid"));
    }

    @Test
    public void failInsert() {
        when(dbService.insertLinkCode(any())).thenReturn(false);

        final Response response = command.run(request);

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.link.phone.request.fail"));
    }

    @Test
    public void sendCode() {
        final Response response = command.run(request);

        verify(twilioClient).sendMessage(eq(DRUNKS_NUMBER), captor.capture());
        final String message = captor.getValue();
        Truth.assertThat(message).isEqualTo(i18nSupport.getLabel("command.link.phone.request", CODE));
        assertThat(response).hasStatus(OK);
    }
}
