package net.spals.drunkr.jersey;

import static com.google.common.truth.Truth.assertThat;

import static com.googlecode.catchexception.throwable.CatchThrowable.catchThrowable;
import static com.googlecode.catchexception.throwable.CatchThrowable.caughtThrowable;
import static org.mockito.Mockito.when;

import javax.ws.rs.NotFoundException;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Unit tests for {@link CheckinConverter}.
 *
 * @author spags
 */
public class CheckinConverterTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final Checkin CHECKIN = new Checkin.Builder()
        .userId(DRINKER.id())
        .name("Duff")
        .producer("Duff Brewery")
        .timestamp(ZonedDateTimes.nowUTC())
        .style(Style.DRAFT)
        .size(Style.DRAFT.getServingSize())
        .abv(.05)
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private CheckinConverter converter;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        converter = new CheckinConverter(dbService, i18nSupport);
    }

    @Test
    public void checkinConverted() {
        when(dbService.getCheckin(CHECKIN.id().toHexString())).thenReturn(Optional.of(CHECKIN));

        final Checkin checkin = converter.fromString(CHECKIN.id().toHexString());

        assertThat(checkin).isEqualTo(CHECKIN);
    }

    @Test
    public void checkinDoesNotExist() {
        when(dbService.getCheckin(CHECKIN.id().toHexString())).thenReturn(Optional.empty());

        catchThrowable(() -> converter.fromString(CHECKIN.id().toHexString()));

        assertThat(caughtThrowable()).hasMessageThat().isEqualTo(i18nSupport.getLabel("invalid.checkin", CHECKIN.id()));
        assertThat(caughtThrowable()).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void checkinToString() {
        final String id = converter.toString(CHECKIN);

        assertThat(id).isEqualTo(CHECKIN.id().toHexString());
    }
}