package net.spals.drunkr.model;

import static com.google.common.truth.Truth.assertThat;

import java.time.ZonedDateTime;

import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;

/**
 * Unit tests for validation and default behavior of {@link JobOptions}.
 *
 * @author spags
 */
public class JobOptionsTest {

    private static final ZonedDateTime NOW = ZonedDateTimes.nowUTC();
    private static final ZonedDateTime PAST = NOW.minusMinutes(5);

    @Test
    public void defaultLastModifiedStartNotSet() {
        final JobOptions options = new JobOptions.Builder()
            .userId(Persons.SPAGS.id())
            .source(Source.SMS)
            .build();

        assertThat(options.startTime()).isGreaterThan(PAST);
        assertThat(options.lastModified()).isEqualTo(options.startTime());
    }

    @Test
    public void defaultLastModifiedStartSet() {
        final JobOptions options = new JobOptions.Builder()
            .userId(Persons.SPAGS.id())
            .source(Source.SMS)
            .startTime(PAST)
            .build();

        assertThat(options.startTime()).isEqualTo(PAST);
        assertThat(options.lastModified()).isEqualTo(PAST);
    }

    @Test
    public void dontOverwriteLastModifiedIfSet() {
        final JobOptions options = new JobOptions.Builder()
            .userId(Persons.SPAGS.id())
            .source(Source.SMS)
            .startTime(NOW)
            .lastModified(PAST)
            .build();

        assertThat(options.startTime()).isEqualTo(NOW);
        assertThat(options.lastModified()).isEqualTo(PAST);
    }
}