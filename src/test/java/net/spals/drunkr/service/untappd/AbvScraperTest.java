package net.spals.drunkr.service.untappd;

import static com.google.common.truth.Truth.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests that Untappd did not change any of their XPaths via making sure we can scrape some beer.
 * This relies on me having a beer in the last month.
 *
 * @author spags
 */
public class AbvScraperTest {

    private static final String WESTFALIA_LINK = "b/fort-point-beer-company-westfalia/574187";
    private AbvScraper scraper;

    @BeforeMethod
    public void setUp() {
        scraper = new AbvScraper();
    }

    @Test(enabled=false)
    public void checkSanity() {
        final double abv = scraper.scrape(WESTFALIA_LINK);

        assertThat(abv).isGreaterThan(0.0);
    }

    @Test(enabled=false)
    public void emptyLink() {
        final double abv = scraper.scrape("");

        assertThat(abv).isEqualTo(0.0);
    }

    @Test(enabled=false)
    public void nullLink() {
        final double abv = scraper.scrape(null);

        assertThat(abv).isEqualTo(0.0);
    }

    @Test(enabled=false)
    public void invalidLink() {
        final double abv = scraper.scrape("badText");

        assertThat(abv).isEqualTo(0.0);
    }
}
