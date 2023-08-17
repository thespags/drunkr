package net.spals.drunkr.service;

import static com.google.common.truth.Truth.assertThat;

import static net.spals.drunkr.model.Gender.MALE;

import java.time.ZonedDateTime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.Gender;

/**
 * Unit tests for {@link BacCalculator}
 *
 * @author spags
 */
public class BacCalculatorTest {

    private BacCalculator calculator;

    @BeforeMethod
    public void setUp() {
        calculator = BacCalculator.get();
    }

    @Test
    public void drunkBac() {
        final double gramsOfAlcohol = calculator.drinkToGramsOfAlcohol(12, .05)
            + calculator.drinkToGramsOfAlcohol(12, .05)
            + calculator.drinkToGramsOfAlcohol(12, .05)
            + calculator.drinkToGramsOfAlcohol(12, .05);
        final double bodyWeight = calculator.poundsToGrams(185);

        final double result = calculator.calculate(gramsOfAlcohol, bodyWeight, MALE, 1);

        assertThat(result).isWithin(.0001).of(.0831);
    }

    @Test
    public void soberBac() {
        final double gramsOfAlcohol = calculator.drinkToGramsOfAlcohol(12, .05);
        final double bodyWeight = calculator.poundsToGrams(185);

        final double result = calculator.calculate(gramsOfAlcohol, bodyWeight, MALE, 4);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    public void checkMaleConstant() {
        final double bodyWeight = calculator.getGenderConstant(Gender.MALE);

        assertThat(bodyWeight).isEqualTo(.68);
    }

    @Test
    public void checkFemaleConstant() {
        final double bodyWeight = calculator.getGenderConstant(Gender.FEMALE);

        assertThat(bodyWeight).isEqualTo(.55);
    }

    @Test
    public void checkPoundsToGrams() {
        final double bodyWeight = calculator.poundsToGrams(185);

        assertThat(bodyWeight).isWithin(.0001).of(83914.52);
    }

    @Test
    public void checkDrinkToGramsOfAlcohol() {
        final double gramsOfAlcohol = calculator.drinkToGramsOfAlcohol(12, .05);

        assertThat(gramsOfAlcohol).isWithin(.0001).of(14);
    }

    @Test
    public void durationInFractionalHours() {
        final ZonedDateTime endTime = ZonedDateTimes.nowUTC();
        final ZonedDateTime startTime = endTime.minusHours(1).minusMinutes(30);

        final double duration = calculator.durationInHours(startTime, endTime);

        assertThat(duration).isEqualTo(1.5);
    }

    @Test
    public void durationInWholeHours() {
        final ZonedDateTime endTime = ZonedDateTimes.nowUTC();
        final ZonedDateTime startTime = endTime.minusHours(3);

        final double duration = calculator.durationInHours(startTime, endTime);

        assertThat(duration).isEqualTo(3.0);
    }
}