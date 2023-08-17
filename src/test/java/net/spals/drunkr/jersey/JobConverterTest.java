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
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;

/**
 * Unit tests for {@link JobConverter}.
 *
 * @author spags
 */
public class JobConverterTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final JobOptions JOB = new Builder()
        .userId(DRINKER.id())
        .source(Source.SMS)
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private JobConverter converter;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        converter = new JobConverter(dbService, i18nSupport);
    }

    @Test
    public void jobConverted() {
        when(dbService.getJob(JOB.id().toHexString())).thenReturn(Optional.of(JOB));

        final JobOptions job = converter.fromString(JOB.id().toHexString());

        assertThat(job).isEqualTo(JOB);
    }

    @Test
    public void jobDoesNotExist() {
        when(dbService.getJob(JOB.id().toHexString())).thenReturn(Optional.empty());

        catchException(() -> converter.fromString(JOB.id().toHexString()));

        final Throwable throwable = caughtException();
        assertThat(throwable).isInstanceOf(NotFoundException.class);
        assertThat(throwable).hasMessageThat().isEqualTo(i18nSupport.getLabel("invalid.job", JOB.id()));
    }

    @Test
    public void jobToString() {
        final String id = converter.toString(JOB);

        assertThat(id).isEqualTo(JOB.id().toHexString());
    }
}