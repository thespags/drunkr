package net.spals.drunkr.service.untappd;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.inject.Inject;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.*;

/**
 * Gets a user's checkin information through Untappd's API.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = CheckinProvider.class, key = "api")
class CheckinApiClient implements CheckinProvider {

    static final String RESPONSE = "response";
    static final String CHECKINS = "checkins";
    static final String ITEMS = "items";
    static final String CHECKIN_ID = "checkin_id";
    static final String RATING_SCORE = "rating_score";
    static final String CREATED_AT = "created_at";
    static final String BEER = "beer";
    static final String BEER_NAME = "beer_name";
    static final String BEER_ABV = "beer_abv";
    static final String BREWERY = "brewery";
    static final String BREWERY_NAME = "brewery_name";
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckinApiClient.class);
    private static final String CHECKINS_URI = "https://api.untappd.com/v4/user/checkins/";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String MAX_ID = "max_id";
    private static final String LIMIT = "limit";
    private static final int LIMIT_SIZE = 50;
    private final KeyStore keyStore;

    @Inject
    CheckinApiClient(final KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    @NotNull
    @Override
    public List<Checkin> get(
        final Optional<UntappdLink> link,
        final Optional<ZonedDateTime> startTime
    ) {
        try {
            // This is saying if we have a link and that link has an access token.
            // Then decrypt and gather all checkins within the start time.
            return link.map(
                x -> x.accessToken().map(
                    accessToken -> ImmutableList.copyOf(
                        new CheckinIterable(
                            ClientBuilder.newClient(),
                            x.userId(),
                            keyStore.decrypt(accessToken),
                            startTime
                        )
                    )
                ).orElse(ImmutableList.of())
            ).orElse(ImmutableList.of());
        } catch (final Throwable x) {
            LOGGER.info("Failed to connect to untappd for checkins", x);
            return ImmutableList.of();
        }
    }

    @VisibleForTesting
    static class CheckinIterable implements Iterable<Checkin> {

        private final Client client;
        private final ObjectId userId;
        private final String accessToken;
        private final Optional<ZonedDateTime> startTime;
        private Checkin next;
        private JsonArray rows;
        private int index;

        @VisibleForTesting
        CheckinIterable(
            final Client client,
            final ObjectId userId,
            final String accessToken,
            final Optional<ZonedDateTime> startTime
        ) {
            this.client = client;
            this.userId = userId;
            this.accessToken = accessToken;
            this.startTime = startTime;

            // Seed the initial start value to start iterating against.
            next = getCheckin();
        }

        @Nonnull
        @Override
        public Iterator<Checkin> iterator() {
            return new Iterator<Checkin>() {

                @Override
                public boolean hasNext() {
                    // If we have no next or its after start time keep pulling more.
                    return next != null && startTime.map(x -> x.isBefore(next.timestamp())).orElse(true);
                }

                @Override
                public Checkin next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final Checkin retValue = next;
                    next = getCheckin();
                    return retValue;
                }
            };
        }

        private Optional<Integer> getLastId() {
            if (rows == null || rows.size() == 0) {
                return Optional.empty();
            }
            final JsonElement row = rows.get(rows.size() - 1);
            final JsonObject checkin = row.getAsJsonObject();
            // max_id is represented as inclusive, and we want exclusive so subtract one.
            final int lastId = checkin.get(CHECKIN_ID).getAsInt() - 1;
            return Optional.of(lastId);
        }

        private Checkin getCheckin() {
            // If we have no rows or looked at all rows grab another batch.
            if (rows == null || index >= rows.size()) {
                final Optional<Integer> lastId = getLastId();
                final String response = getCheckinsJson(lastId);
                rows = new JsonParser().parse(response)
                    .getAsJsonObject()
                    .getAsJsonObject(RESPONSE)
                    .getAsJsonObject(CHECKINS)
                    .getAsJsonArray(ITEMS);
                index = 0;
            }
            // This batch contains no rows so return null.
            if (rows.size() == 0) {
                return null;
            }
            final JsonElement row = rows.get(index++);
            final JsonObject checkin = row.getAsJsonObject();
            return parseCheckin(checkin);
        }

        private Checkin parseCheckin(final JsonObject checkin) {
            // Currently we are representing rating as an integer because the UI uses an integer.
            final int rating = (int) checkin.get(RATING_SCORE).getAsDouble() * 100;
            final ZonedDateTime dateTime = ZonedDateTimes.parseUntappd(checkin.get(CREATED_AT).getAsString());

            final JsonObject beer = checkin.getAsJsonObject(BEER);
            final String name = beer.get(BEER_NAME).getAsString();
            // Convert ABV from a percentage.
            final double abv = beer.get(BEER_ABV).getAsDouble() / 100;

            final JsonObject brewery = checkin.getAsJsonObject(BREWERY);
            final String producer = brewery.get(BREWERY_NAME).getAsString();

            return new Checkin.Builder()
                .name(name)
                .userId(userId)
                .producer(producer)
                .rating(rating)
                .style(Style.NONE)
                .size(Style.NONE.getServingSize())
                .timestamp(dateTime)
                .abv(abv)
                .build();
        }

        /**
         * Get the user's checkins, user name is not needed if access_token is provided.
         *
         * @param maxId optional id for when you want results to start, inclusive
         * @return json representing the user's checkin history
         */
        private String getCheckinsJson(final Optional<Integer> maxId) {
            final UriBuilder uriBuilder = UriBuilder.fromUri(CHECKINS_URI)
                .queryParam(LIMIT, LIMIT_SIZE)
                .queryParam(ACCESS_TOKEN, accessToken);
            maxId.map(x -> uriBuilder.queryParam(MAX_ID, x));

            return client.target(uriBuilder)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        }
    }
}
