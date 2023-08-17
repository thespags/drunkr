package net.spals.drunkr.service.untappd;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.service.untappd.CheckinApiClient.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.Checkin;
import net.spals.drunkr.model.Persons;

/**
 * A maybe complex test suite to create parsable json to give to {@link CheckinIterable} for logical verification.
 *
 * @author spags
 */
public class CheckinIterableTest {

    private static final String EMPTY_CHECKINS = buildCheckins();
    private static final ObjectId USER_ID = Persons.SPAGS.id();
    private static final String ACCESS_TOKEN = "accessToken";
    @Mock
    private Client client;
    @Mock
    private WebTarget target;
    @Mock
    private Invocation.Builder targetBuilder;
    private ZonedDateTime now;

    private static String buildBeerJson(final String name, final double abv) {
        return "\"" + BEER + "\": {"
            + "\n\t\t\"" + BEER_NAME + "\": \"" + name + "\", "
            + "\n\t\t\"" + BEER_ABV + "\": " + abv
            + "\n\t}";
    }

    private static String buildBreweryJson(final String name) {
        return "\"" + BREWERY + "\": {"
            + "\n\t\t\"" + BREWERY_NAME + "\": \"" + name + "\""
            + "\n\t}";
    }

    private static String buildCheckin(
        final int id,
        final double rating,
        final ZonedDateTime timestamp,
        final String beer,
        final String brewery
    ) {
        return "\n\t\"" + CHECKIN_ID + "\": " + id + ", "
            + "\n\t\"" + RATING_SCORE + "\": " + rating + ", "
            + "\n\t\"" + CREATED_AT + "\": \"" + ZonedDateTimes.formatUntappd(timestamp) + "\", "
            + "\n\t" + beer + ","
            + "\n\t" + brewery;
    }

    private static String buildCheckins(final String... checkins) {
        // If empty array then use empty string to avoid {}
        final String checkinsJson = checkins.length == 0
            ? ""
            : Arrays.stream(checkins).collect(Collectors.joining("\n},{", "\n{", "\n}"));
        return "{"
            + "\n\"" + RESPONSE + "\" : {"
            + "\n\"" + CHECKINS + "\" : {"
            + "\n\"" + ITEMS + "\" : ["
            + checkinsJson
            + "\n]"
            + "\n}"
            + "\n}"
            + "\n}";
    }

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(client.target(any(UriBuilder.class))).thenReturn(target);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(targetBuilder);
        now = ZonedDateTimes.nowUTC();
    }

    @Test
    public void multipleCheckinsSingleBatch() {
        final String json = buildCheckins(
            buildCheckin(
                15,
                4.0,
                now,
                buildBeerJson("Duff Lite", 5.0),
                buildBreweryJson("Duff")
            ),
            buildCheckin(
                13,
                3.0,
                now.minusMinutes(1),
                buildBeerJson("Brock Extra Special", 4.0),
                buildBreweryJson("Brock")
            )
        );
        // We must follow with empty batch to terminate the iterable.
        when(targetBuilder.get(String.class))
            .thenReturn(json)
            .thenReturn(EMPTY_CHECKINS);

        final Iterable<Checkin> checkins = new CheckinIterable(
            client,
            USER_ID,
            ACCESS_TOKEN,
            Optional.empty()
        );

        assertThat(checkins).hasSize(2);
    }

    @Test
    public void multipleCheckinsMultipleBatches() {
        final String firstBatch = buildCheckins(
            buildCheckin(
                15,
                4.0,
                now,
                buildBeerJson("Duff Lite", 5.0),
                buildBreweryJson("Duff")
            )
        );
        final String secondBatch = buildCheckins(
            buildCheckin(
                13,
                3.0,
                now.minusMinutes(1),
                buildBeerJson("Brock Extra Special", 4.0),
                buildBreweryJson("Brock")
            )
        );
        // We must follow with empty batch to terminate the iterable.
        when(targetBuilder.get(String.class))
            .thenReturn(firstBatch)
            .thenReturn(secondBatch)
            .thenReturn(EMPTY_CHECKINS);

        final Iterable<Checkin> checkins = new CheckinIterable(
            client,
            USER_ID,
            ACCESS_TOKEN,
            Optional.empty()
        );

        assertThat(checkins).hasSize(2);
    }

    @Test
    public void multipleCheckinsFilteredByStartTime() {
        final String json = buildCheckins(
            buildCheckin(
                15,
                4.0,
                now,
                buildBeerJson("Duff Lite", 5.0),
                buildBreweryJson("Duff")
            ),
            buildCheckin(
                13,
                3.0,
                now.minusMinutes(5),
                buildBeerJson("Brock Extra Special", 4.0),
                buildBreweryJson("Brock")
            )
        );
        // We must follow with empty batch to terminate the iterable.
        when(targetBuilder.get(String.class))
            .thenReturn(json)
            .thenReturn(EMPTY_CHECKINS);

        final Iterable<Checkin> checkins = new CheckinIterable(
            client,
            USER_ID,
            ACCESS_TOKEN,
            Optional.of(now.minusMinutes(2))
        );

        assertThat(checkins).hasSize(1);
    }
}