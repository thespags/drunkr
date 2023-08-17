package net.spals.drunkr.jersey;

import static com.google.common.truth.Truth.assertThat;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.mockito.Mockito.when;

import javax.ws.rs.NotFoundException;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.model.Persons;

/**
 * Unit tests for {@link PersonConverter}.
 *
 * @author spags
 */
public class PersonConverterTest {

    private static final Person DRINKER = Persons.SPAGS;
    private static final String DRINKER_NUMBER = Persons.SPAGS_NUMBER;
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private PersonConverter converter;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        converter = new PersonConverter(dbService, i18nSupport);
    }

    @Test
    public void userConverted() {
        when(dbService.getPerson(DRINKER.id().toHexString())).thenReturn(Optional.of(DRINKER));

        final Person person = converter.fromString(DRINKER.id().toHexString());

        assertThat(person).isEqualTo(DRINKER);
    }

    @Test
    public void userDoesNotExist() {
        when(dbService.getPerson(DRINKER.id().toHexString())).thenReturn(Optional.empty());

        catchException(() -> converter.fromString(DRINKER.id().toHexString()));

        final Throwable throwable = caughtException();
        assertThat(throwable).isInstanceOf(NotFoundException.class);
        assertThat(throwable).hasMessageThat().isEqualTo(i18nSupport.getLabel("invalid.user", DRINKER.id()));
    }

    @Test
    public void userToString() {
        final String id = converter.toString(DRINKER);

        assertThat(id).isEqualTo(DRINKER.id().toHexString());
    }

    @Test
    public void translatePhone() {
        when(dbService.getPerson(DRINKER_NUMBER)).thenReturn(Optional.of(DRINKER));

        final Person person = converter.fromString("4122513259");

        assertThat(person).isEqualTo(DRINKER);
    }
}