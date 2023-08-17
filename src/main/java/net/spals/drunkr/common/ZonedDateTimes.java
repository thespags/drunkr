package net.spals.drunkr.common;

import java.time.*;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Date conversion helper.
 *
 * @author spags
 * @author jbrock
 */
public class ZonedDateTimes {

    private static final DateTimeFormatter API_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter CHECKIN_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
    private static final String AMERICA_LOS_ANGELES = "America/Los_Angeles";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:mma yyyy-MM-dd");
    public static final ZoneId UTC = ZoneId.of("UTC");

    private ZonedDateTimes() {
    }

    public static ZonedDateTime nowUTC() {
        return ZonedDateTime.now(UTC);
    }

    public static ZonedDateTime convert(final Date timestamp) {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), UTC);
    }

    /**
     * Parses date times of the form Fri, 23 Dec 2017 16:17:29 +0000, ensuring UTC format.
     *
     * @param rawDateTime the date time as a string
     * @return the parsed date as a {@link ZonedDateTime}
     */
    public static ZonedDateTime parseUntappd(final String rawDateTime) {
        return ZonedDateTime.parse(rawDateTime, CHECKIN_FORMAT)
            .withZoneSameInstant(ZonedDateTimes.UTC);
    }

    /**
     * Prints date times as the form Fri, 23 Dec 2017 16:17:29 +0000.
     *
     * @param timestamp the date time to be formatted
     * @return the formatted date time
     */
    public static String formatUntappd(final ZonedDateTime timestamp) {
        return timestamp.format(CHECKIN_FORMAT);
    }

    public static Date convert(final ZonedDateTime timestamp) {
        return Date.from(timestamp.toInstant());
    }

    public static String formatPacific(final ZonedDateTime timestamp) {
        return timestamp.withZoneSameInstant(ZoneId.of(AMERICA_LOS_ANGELES))
            .format(FORMATTER);
    }

    /**
     * Returns true if the first timestamp is on or after the second timestamp.
     * Equivalent to the not of {@link ZonedDateTime#isBefore(ChronoZonedDateTime)}.
     * <p>
     * Unfortunately this doesn't read fluent in code as first.isOnAfter(second),
     * but read this method as "first is on or after the second".
     *
     * @param first  the left hand sign of the operation
     * @param second the right hand sign of the operation
     * @return true if the first timestamp is on or after the second otherwise false
     */
    public static boolean isOnOrAfter(final ZonedDateTime first, final ZonedDateTime second) {
        return !first.isBefore(second);
    }

    /**
     * Parses date times of the form 2017-12-25T16:17:29.000Z, i.e. RFC3339. This will convert to UTC.
     *
     * @param rawDateTime the date time as a string
     * @return the parsed date as a {@link ZonedDateTime}
     */
    public static ZonedDateTime parseApi(final String rawDateTime) {
        return ZonedDateTime.parse(rawDateTime, API_FORMAT)
            .withZoneSameInstant(UTC);
    }

    public static String formatApi(final ZonedDateTime timestamp) {
        return timestamp.format(API_FORMAT);
    }
}
