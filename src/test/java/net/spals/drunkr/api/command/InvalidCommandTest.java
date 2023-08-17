package net.spals.drunkr.api.command;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import static net.spals.drunkr.common.ResponseSubject.assertThat;

import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableMap;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;

/**
 * Tests for {@link InvalidCommand}.
 *
 * @author spags
 */
public class InvalidCommandTest {

    private I18nSupport i18nSupport;
    private InvalidCommand command;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        command = new InvalidCommand(i18nSupport);
    }

    @Test
    public void checkInvalid() {
        final Response response = command.run(ImmutableMap.of());

        assertThat(response)
            .hasStatus(INTERNAL_SERVER_ERROR)
            .hasErrorMessage(i18nSupport.getLabel("command.invalid"));
    }
}