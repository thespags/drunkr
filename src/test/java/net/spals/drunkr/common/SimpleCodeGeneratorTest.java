package net.spals.drunkr.common;

import static com.google.common.truth.Truth.assertThat;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link SimpleCodeGenerator}.
 *
 * @author spags
 */
public class SimpleCodeGeneratorTest {

    @Test
    public void forceGenerateNumeric() {
        final SimpleCodeGenerator generator = new SimpleCodeGenerator(x -> 0);
        generator.setAlphabet(0);

        final String code = generator.generate();

        assertThat(code).isEqualTo("000000");
    }

    @Test
    public void forceGenerateLowerAlpha() {
        final SimpleCodeGenerator generator = new SimpleCodeGenerator(x -> 0);
        generator.setAlphabet(1);

        final String code = generator.generate();

        assertThat(code).isEqualTo("aaaaaa");
    }

    @Test
    public void forceGenerateAlpha() {
        final SimpleCodeGenerator generator = new SimpleCodeGenerator(x -> 26);
        generator.setAlphabet(2);

        final String code = generator.generate();

        assertThat(code).isEqualTo("AAAAAA");
    }

    @Test
    public void forceGenerateLowerAlphaNumeric() {
        final SimpleCodeGenerator generator = new SimpleCodeGenerator(x -> x - 3);
        generator.setAlphabet(1);

        final String code = generator.generate();

        assertThat(code).isEqualTo("xxxxxx");
    }

    @Test
    public void forceGenerateAlphaNumeric() {
        final SimpleCodeGenerator generator = new SimpleCodeGenerator(x -> x - 3);
        generator.setAlphabet(2);

        final String code = generator.generate();

        assertThat(code).isEqualTo("XXXXXX");
    }
}