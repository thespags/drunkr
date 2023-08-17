package net.spals.drunkr.common;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * Creates a simple 6 digit code.
 *
 * @author spags
 */
@AutoBindSingleton(baseClass = CodeGenerator.class)
public class SimpleCodeGenerator implements CodeGenerator {

    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final Function<Integer, Integer> function;
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("code.length")
    private int length = 6;
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("code.alphabet")
    private int codeAlphabet = 0;
    private String alphabet;

    @Inject
    SimpleCodeGenerator() {
        this(RANDOM::nextInt);
    }

    /**
     * Allows a non random generator for testing.
     */
    @VisibleForTesting
    SimpleCodeGenerator(final Function<Integer, Integer> function) {
        this.function = function;
    }

    @PostConstruct
    private void setAlphabet() {
        setAlphabet(codeAlphabet);
    }

    @VisibleForTesting
    void setAlphabet(final int codeAlphabet) {
        switch (codeAlphabet) {
        case 0:
            alphabet = NUMBERS;
            break;
        case 1:
            alphabet = LETTERS;
            break;
        case 2:
            alphabet = LETTERS + LETTERS.toUpperCase();
            break;
        case 3:
            alphabet = NUMBERS + LETTERS;
            break;
        case 4:
            alphabet = NUMBERS + LETTERS + LETTERS.toUpperCase();
            break;
        default:
            alphabet = NUMBERS;
            break;
        }
    }

    @Override
    public String generate() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(function.apply(alphabet.length())));
        }
        return builder.toString();
    }
}
