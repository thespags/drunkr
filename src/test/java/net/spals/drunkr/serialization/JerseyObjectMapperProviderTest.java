package net.spals.drunkr.serialization;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.time.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;

/**
 * Unit tests for configuration of {@link JerseyObjectMapperProvider}
 *
 * @author spags
 */
public class JerseyObjectMapperProviderTest {

    private ObjectMapper mapper;

    @BeforeMethod
    public void setUp() {
        mapper = new JerseyObjectMapperProvider().getContext(JerseyObjectMapperProvider.class);
    }

    @Test
    public void serializeZonedDateTime() throws JsonProcessingException {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final long epoch = now.toInstant().toEpochMilli();

        final Long actual = Long.valueOf(mapper.writeValueAsString(now));

        assertThat(actual).isEqualTo(epoch);
    }

    @Test
    public void deserializeZonedDateTimeFromMilliseconds() throws IOException {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final long epoch = now.toInstant().toEpochMilli();

        final ZonedDateTime actual = mapper.readValue(String.valueOf(epoch), ZonedDateTime.class);

        assertThat(actual).isEqualTo(now);
    }

    @Test
    public void deserializeZonedDateTimeFromString() throws IOException {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final String formatted = ZonedDateTimes.formatApi(now);

        final ZonedDateTime actual = mapper.readValue("\"" + formatted + "\"", ZonedDateTime.class);

        assertThat(actual).isEqualTo(now);
    }
}