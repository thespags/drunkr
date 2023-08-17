package net.spals.drunkr.jersey;

import static com.google.common.truth.Truth.assertThat;

import java.time.ZonedDateTime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;

/**
 * Unit tests for {@link ZonedDateTime}.
 *
 * @author spags
 */
public class ZonedDateTimeConverterTest {

    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(2017, 12, 25, 23, 25, 5, 0, ZonedDateTimes.UTC);
    private static final String FORMAT = "2017-12-25T23:25:05Z";
    private ZonedDateTimeConverter converter;

    @BeforeMethod
    public void setUp() {
        converter = new ZonedDateTimeConverter();
    }

    @Test
    public void zonedDateTimeConverted() {
        final ZonedDateTime timestamp = converter.fromString(FORMAT);

        assertThat(timestamp).isEqualTo(TIMESTAMP);
    }

    @Test
    public void zonedDaetTimeToString() {
        final String format = converter.toString(TIMESTAMP);

        assertThat(format).isEqualTo(FORMAT);
    }
}