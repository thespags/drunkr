package net.spals.drunkr.service.untappd;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import static org.mockito.Mockito.*;

import java.time.*;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import com.netflix.governator.configuration.ConfigurationKey;
import com.typesafe.config.*;
import org.bson.types.ObjectId;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

import net.spals.appbuilder.config.provider.TypesafeConfigurationProvider;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.*;

/**
 * Tests that Untappd did not change any of their XPaths via making sure we can scrape some beer.
 * These test rely on me having a beer in the last month.
 *
 * @author spags
 */
public class CheckinScraperITest {

    private static final UntappdLink VALID_LINK = new UntappdLink.Builder()
        .userId(new ObjectId())
        // Spags' username is the same as spags' untappd username.
        .untappdName(Persons.SPAGS.userName())
        .build();
    private static final UntappdLink INVALID_LINK = new UntappdLink.Builder()
        .userId(new ObjectId())
        .untappdName("spags1729foobar")
        .build();
    private WebDriver driver;
    private AbvScraper abvScraper;
    private LoginDriver loginDriver;
    private CheckinScraper scraper;

    @BeforeClass
    public void classSetUp() {
        final Config config = ConfigFactory.load(
            "config/drunkr.conf",
            ConfigParseOptions.defaults().setAllowMissing(false),
            ConfigResolveOptions.defaults()
        );
        final TypesafeConfigurationProvider provider = new TypesafeConfigurationProvider(config);
        final ConfigurationKey userKey = new ConfigurationKey("untappd.master.user", ImmutableList.of());
        final String user = provider.getStringProperty(userKey, "").get();
        final ConfigurationKey passwordKey = new ConfigurationKey("untappd.master.password", ImmutableList.of());
        final String password = provider.getStringProperty(passwordKey, "").get();

        abvScraper = new AbvScraper();
        loginDriver = new LoginDriver();
        loginDriver.setConfiguration(user, password);
        scraper = new CheckinScraper(abvScraper, loginDriver);
        driver = scraper.createWebDriver();
    }

    @AfterClass
    public void classTearDown() {
        driver.quit();
    }

    @Test(enabled=false)
    public void checkValidSanity() {
        final List<Checkin> checkins = scraper.get(
            Optional.of(VALID_LINK),
            Optional.of(ZonedDateTimes.nowUTC().minusMonths(1))
        );

        assertThat(checkins).isNotEmpty();
    }

    @Test(enabled=false)
    public void checkInvalidSanity() {
        final List<Checkin> checkins = scraper.get(
            Optional.of(INVALID_LINK),
            Optional.of(ZonedDateTimes.nowUTC().minusMonths(1))
        );

        assertThat(checkins).isEmpty();
    }

    @Test(enabled=false)
    public void invalidUntappdName() {
        scraper.openPersonPage(driver, INVALID_LINK);

        final boolean isValidPerson = scraper.isValidPerson(driver);

        assertThat(isValidPerson).isEqualTo(false);
    }

    @Test(enabled=false)
    public void emptyUntappdLink() {
        // Create a spy to verify no web driver was created. Don't set to the field to remain idempotent.
        final CheckinScraper scraper = spy(new CheckinScraper(abvScraper, loginDriver));

        // Don't open a page but try clicking, which we won't do.
        final List<Checkin> checkins = scraper.get(Optional.empty(), Optional.empty());

        assertThat(checkins).isEmpty();
        verify(scraper, never()).createWebDriver();
    }

    @Test(enabled=false)
    public void validUntappdName() {
        scraper.openPersonPage(driver, VALID_LINK);

        final boolean isValidPerson = scraper.isValidPerson(driver);

        assertThat(isValidPerson).isEqualTo(true);
    }

    @Test(enabled=false)
    public void notClickingNeededSanity() {
        scraper.openPersonPage(driver, VALID_LINK);
        final Optional<ZonedDateTime> firstLastCheckin = scraper.getLastCheckin(driver);
        scraper.needToClick(firstLastCheckin, Optional.of(ZonedDateTimes.nowUTC()));

        final Optional<ZonedDateTime> finalLastCheckin = scraper.getLastCheckin(driver);

        assertThat(firstLastCheckin).isEqualTo(finalLastCheckin);
    }

    @Test(enabled=false)
    public void clickingSanity() {
        scraper.openPersonPage(driver, VALID_LINK);
        final Optional<ZonedDateTime> firstLastCheckin = scraper.getLastCheckin(driver);
        scraper.clickNextUntilDateTime(driver, Optional.of(ZonedDateTimes.nowUTC().minusMonths(6)));

        final Optional<ZonedDateTime> finalLastCheckin = scraper.getLastCheckin(driver);
        assertThat(firstLastCheckin).isNotEqualTo(finalLastCheckin);
    }

    @Test(enabled=false)
    public void timestampsAreUTC() {
        scraper.openPersonPage(driver, VALID_LINK);
        final Optional<ZonedDateTime> firstLastCheckin = scraper.getLastCheckin(driver);

        final ZoneId zone = firstLastCheckin.map(ZonedDateTime::getZone).orElseThrow(AssertionError::new);
        assertThat(zone).isEqualTo(ZoneOffset.UTC);
    }
}