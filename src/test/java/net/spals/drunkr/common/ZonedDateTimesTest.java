package net.spals.drunkr.common;

import static com.google.common.truth.Truth.assertThat;

import java.time.ZonedDateTime;
import java.util.Date;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link ZonedDateTimes} verifying custom behavior.
 *
 * @author jbrock
 */
public class ZonedDateTimesTest {

    @Test
    public void nowUTC() {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();

        assertThat(now.getZone()).isEqualTo(ZonedDateTimes.UTC);
    }

    @Test
    public void parseApi() {
        final ZonedDateTime expected = ZonedDateTime.of(2017, 12, 25, 23, 25, 5, 0, ZonedDateTimes.UTC);

        final ZonedDateTime actual = ZonedDateTimes.parseApi("2017-12-25T23:25:05.000Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void parseApiNonUTC() {
        final ZonedDateTime expected = ZonedDateTime.of(2017, 12, 25, 23, 25, 5, 0, ZonedDateTimes.UTC);

        final ZonedDateTime actual = ZonedDateTimes.parseApi("2017-12-25T21:25:05.000-02:00");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void parseUntappd() {
        final ZonedDateTime expected = ZonedDateTime.of(2017, 12, 3, 23, 25, 5, 0, ZonedDateTimes.UTC);

        final ZonedDateTime actual = ZonedDateTimes.parseUntappd("Sun, 03 Dec 2017 23:25:05 +0000");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void parseUntappdNonUTC() {
        final ZonedDateTime expected = ZonedDateTime.of(2017, 12, 3, 23, 25, 5, 0, ZonedDateTimes.UTC);

        final ZonedDateTime actual = ZonedDateTimes.parseUntappd("Sun, 03 Dec 2017 21:25:05 -0200");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void formatUntappd() {
        final ZonedDateTime timestamp = ZonedDateTime.of(2017, 12, 3, 23, 25, 5, 0, ZonedDateTimes.UTC);

        final String actual = ZonedDateTimes.formatUntappd(timestamp);

        assertThat(actual).isEqualTo("Sun, 03 Dec 2017 23:25:05 +0000");
    }

    @Test
    public void formatPacific() {
        final ZonedDateTime timestamp = ZonedDateTime.of(2017, 12, 3, 23, 25, 5, 0, ZonedDateTimes.UTC);

        final String format = ZonedDateTimes.formatPacific(timestamp);

        assertThat(format).isEqualTo("03:25PM 2017-12-03");
    }

    @Test
    public void dateToZonedDateTime() {
        final Date now = new Date();

        final ZonedDateTime zonedNow = ZonedDateTimes.convert(now);

        assertThat(zonedNow.toInstant()).isEqualTo(now.toInstant());
    }

    @Test
    public void zonedDateTimeToDate() {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();

        final Date date = ZonedDateTimes.convert(now);

        assertThat(date.toInstant()).isEqualTo(now.toInstant());
    }

    @Test
    public void isOnOrAfterBefore() {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final ZonedDateTime after = now.plusDays(1);

        final boolean isOnOrAfter = ZonedDateTimes.isOnOrAfter(now, after);

        assertThat(isOnOrAfter).isEqualTo(false);
    }

    @Test
    public void isOnOrAfterEqual() {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();

        final boolean isOnOrAfter = ZonedDateTimes.isOnOrAfter(now, now);

        assertThat(isOnOrAfter).isEqualTo(true);
    }

    @Test
    public void isOnOrAfterAfter() {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final ZonedDateTime after = now.plusDays(1);

        final boolean isOnOrAfter = ZonedDateTimes.isOnOrAfter(after, now);

        assertThat(isOnOrAfter).isEqualTo(true);
    }
}
