package net.spals.drunkr.common;

import java.util.Optional;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Utility for unifying phone numbers from user input.
 *
 * @author spags
 */
public class PhoneNumbers {

    private static final PhoneNumberUtil INSTANCE = PhoneNumberUtil.getInstance();

    private PhoneNumbers() {
    }

    /**
     * Try and parse a phone number and convert it to {@link PhoneNumberFormat#E164}, e.g. +14122513259,
     * which is our standard way of storing phone numbers. If the number did not parse then return the original value.
     * For instance we allow lookups by untappd user name.
     *
     * @return formatted phone number if a valid phone number otherwise the input
     */
    public static String tryParse(final String phoneNumber) {
        try {
            final PhoneNumber number = INSTANCE.parse(phoneNumber, "US");
            return PhoneNumberUtil.getInstance().format(number, PhoneNumberFormat.E164);
        } catch (final Throwable x) {
            return phoneNumber;
        }
    }

    /**
     * Similar to {@link #tryParse(String)} however this must be a phone number.
     *
     * @return phone number or empty if its not a valid US phone number
     */
    public static Optional<String> parse(final String phoneNumber, final String defaultRegion) {
        try {
            final PhoneNumber number = PhoneNumberUtil.getInstance().parse(phoneNumber, defaultRegion);
            return Optional.of(PhoneNumberUtil.getInstance().format(number, PhoneNumberFormat.E164));
        } catch (final Throwable x) {
            return Optional.empty();
        }
    }
}
