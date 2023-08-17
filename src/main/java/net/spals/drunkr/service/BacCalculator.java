package net.spals.drunkr.service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.google.common.base.Preconditions;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.Checkin;
import net.spals.drunkr.model.Gender;

/**
 * Uses the 'Widmark Formula' to calculate blood alcohol content or BAC.
 * See <a href='https://www.wikihow.com/Calculate-Blood-Alcohol-Content-(Widmark-Formula)'>formula</a> for more details.
 *
 * @author spags
 */
class BacCalculator {

    private static final BacCalculator INSTANCE = new BacCalculator();
    private static final double MILLISECONDS_IN_AN_HOUR = ChronoUnit.HOURS.getDuration().toMillis();

    private BacCalculator() {
    }

    static BacCalculator get() {
        return INSTANCE;
    }

    /**
     * @param gramsOfAlcohol grams of alcohol consumed
     * @param bodyWeight     weight of the individual in grams
     * @param gender         gender of the individual
     * @param elapsedTime    elapsed time in hours
     * @return blood alcohol content of the individual
     */
    double calculate(
        final double gramsOfAlcohol,
        final double bodyWeight,
        final Gender gender,
        final double elapsedTime
    ) {
        final double bac = gramsOfAlcohol / (bodyWeight * getGenderConstant(gender)) * 100 - elapsedTime * .015;
        return Math.max(bac, 0.0);
    }

    /**
     * The water density constant of a gender.
     *
     * @param gender gender of the individual
     * @return water density constant
     */
    double getGenderConstant(final Gender gender) {
        switch (gender) {
        case MALE:
            return .68;
        case FEMALE:
            return .55;
        }
        throw new IllegalArgumentException("Unknown gender: " + gender);
    }

    /**
     * Converts a drink in fluid ounces and alcohol percentage to grams of alcohol.
     * <pre>
     * One US fluid ounce ≈ 29.5735 ml
     * 1 ml of water = 1 gram of weight
     * Ethanol density ≈ 0.789 g/cm3
     * </pre>
     *
     * @param volume in fluid ounces
     * @param abv    percentage of alcohol, e.g. .05
     * @return grams of alcohol
     */
    double drinkToGramsOfAlcohol(final double volume, final double abv) {
        return volume * 29.5735 * abv * .789;
    }

    double drinkToGramsOfAlcohol(final Checkin checkin) {
        return drinkToGramsOfAlcohol(checkin.size(), checkin.abv());
    }

    /**
     * Converts weight in pounds to weight in grams
     *
     * @param pounds weight in pounds
     * @return weight in grams
     */
    double poundsToGrams(final double pounds) {
        return pounds * 453.592;
    }

    /**
     * Calculates the fractional hours between a start time and the current time
     *
     * @param startTime time at which the event started
     * @return fractional hours represented as a double between start time and now
     */
    double durationInHours(final ZonedDateTime startTime) {
        return durationInHours(startTime, ZonedDateTimes.nowUTC());
    }

    /**
     * Calculates the fractional hours between a start time and the end time.
     *
     * @param startTime the start instant, inclusive, not null
     * @param endTime   the end instant, exclusive, not null
     * @return fractional hours represented as a double between start time and the end time
     */
    double durationInHours(final ZonedDateTime startTime, final ZonedDateTime endTime) {
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        Preconditions.checkState(
            ZonedDateTimes.isOnOrAfter(endTime, startTime),
            "endTime cannot be before startTime: startTime=" + startTime + " endTime=" + endTime
        );
        return Duration.between(startTime, endTime).toMillis() / MILLISECONDS_IN_AN_HOUR;
    }
}
