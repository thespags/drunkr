package net.spals.drunkr.model;

import static com.google.common.truth.Truth.assertThat;

import org.testng.annotations.Test;

/**
 * Tests for {@link Style}.
 *
 * @author spags
 */
public class StyleTest {

    @Test
    public void caseSensitiveFound() {
        final Style style = Style.get("Draft");

        assertThat(style).isEqualTo(Style.DRAFT);
    }

    @Test
    public void caseSensitiveNotFound() {
        final Style style = Style.get("draft");

        assertThat(style).isEqualTo(Style.NONE);
    }

    @Test
    public void caseSensitiveNull() {
        final Style style = Style.get(null);

        assertThat(style).isEqualTo(Style.NONE);
    }

    @Test
    public void caseInsensitiveFound() {
        final Style style = Style.getInsensitive("draft");

        assertThat(style).isEqualTo(Style.DRAFT);
    }

    @Test
    public void caseInsensitiveNotFound() {
        final Style style = Style.getInsensitive("foo");

        assertThat(style).isEqualTo(Style.NONE);
    }

    @Test
    public void caseInsensitiveNull() {
        final Style style = Style.getInsensitive(null);

        assertThat(style).isEqualTo(Style.NONE);
    }
}