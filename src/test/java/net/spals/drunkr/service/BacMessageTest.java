package net.spals.drunkr.service;

import static com.google.common.truth.Truth.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;

/**
 * Unit tests for {@link BacMessage}
 *
 * @author spags
 */
public class BacMessageTest {

    private I18nSupport i18nSupport;
    private BacMessage bacMessage;

    @BeforeMethod
    public void setUp() {
        i18nSupport = I18nSupports.getEnglish();
        bacMessage = new BacMessage(i18nSupport);
    }

    @Test
    public void sober() {
        final String message = bacMessage.get(.00);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_sober"));
    }

    @Test
    public void basicallySober() {
        final String message = bacMessage.get(.01);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_basically_sober"));
    }

    @Test
    public void lightHeaded() {
        final String message = bacMessage.get(.04);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_lightheaded"));
    }

    @Test
    public void buzzed() {
        final String message = bacMessage.get(.07);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_buzzed"));
    }

    @Test
    public void legallyImpaired() {
        final String message = bacMessage.get(.08);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_legally_impaired"));
    }

    @Test
    public void drunk() {
        final String message = bacMessage.get(.12);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_drunk"));
    }

    @Test
    public void veryDrunk() {
        final String message = bacMessage.get(.18);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_very_drunk"));
    }

    @Test
    public void dazedAndConfused() {
        final String message = bacMessage.get(.22);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_dazed_and_confused"));
    }

    @Test
    public void stupor() {
        final String message = bacMessage.get(.27);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_stupor"));
    }

    @Test
    public void coma() {
        final String message = bacMessage.get(.32);

        assertThat(message).isEqualTo(i18nSupport.getLabel("level_coma"));
    }
}