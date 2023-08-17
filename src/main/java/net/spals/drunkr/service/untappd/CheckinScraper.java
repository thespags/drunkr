package net.spals.drunkr.service.untappd;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.xsoup.XPathEvaluator;
import us.codecraft.xsoup.Xsoup;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.*;

/**
 * Scrapes a user's Untappd checkins by scraping their user page. This drives to the user's page.
 * Then clicks "next" until all checkins are within the required start time if any, otherwise clicks until it can't click.
 * Then we parse the html into {@link Checkin}, returning the list in reverse chronological order.
 * We click first, then parse so we can grab the page source in a single hit. Otherwise we would have to consider
 * a subset of page source at a time.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = CheckinProvider.class, key = "scraper")
class CheckinScraper implements CheckinProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckinScraper.class);
    private static final String UNTAPPD_URL = "https://untappd.com/user/";
    private static final By NEXT_PATH = By.xpath("//*[@id=\"slide\"]/div[1]/div[3]/div/div/a");
    private static final By LAST_CHECKIN_PATH = By.xpath("//div[@class=\"item\"][last()]/div[2]/div[2]/div[2]/a[1]");
    private static final By ERROR_MESSAGE_PATH = By.xpath("//*[@id=\"maintenance\"]/h1");
    private static final XPathEvaluator BEER_NAME_PATH = Xsoup.compile("//div[2]/div[1]/p/a[2]");
    private static final XPathEvaluator BREWERY_PATH = Xsoup.compile("//div[2]/div[1]/p/a[3]/text()");
    private static final XPathEvaluator CHECKIN_TIME_STAMP_PATH = Xsoup.compile(
        "//div[2]/div[2]/div[2]/a[1]/@data-gregtime"
    );
    private static final XPathEvaluator RATING_PATH = Xsoup.compile("//div[2]/div[1]/div/div/span/@class");
    private static final XPathEvaluator STYLE_PATH = Xsoup.compile("//div[2]/div[1]/div/div/p/span/text()");
    private static final XPathEvaluator ITEM_PATH = Xsoup.compile("//div[@class=\"item\"]");
    private final AbvProvider abvScraper;
    private final LoginDriver loginDriver;

    @Inject
    CheckinScraper(final AbvProvider abvScraper, final LoginDriver loginDriver) {
        this.abvScraper = abvScraper;
        this.loginDriver = loginDriver;
    }

    /**
     * Scrapes from the a user's page for all checkins or checkins with the last provided date span.
     */
    @NotNull
    @Override
    public List<Checkin> get(final Optional<UntappdLink> link, final Optional<ZonedDateTime> untilDateTime) {
        // Untappd is an optional value if its not set don't bother setting up web driver or clicking or anything.
        if (!link.isPresent()) {
            return ImmutableList.of();
        }

        try (final CloseableWebDriver closeableWebDriver = new CloseableWebDriver(createWebDriver())) {
            final WebDriver driver = closeableWebDriver.driver;
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);

            openPersonPage(driver, link.get());

            // If Untappd user name became invalid check now.
            if (!isValidPerson(driver)) {
                return ImmutableList.of();
            }

            final boolean needToClick = needToClick(getLastCheckin(driver), untilDateTime);
            if (needToClick) {
                LOGGER.info("starting clicking");
                clickNextUntilDateTime(driver, untilDateTime);
            } else {
                LOGGER.info("no need to click");
            }

            LOGGER.info("starting to parse source");
            final Document document = Jsoup.parse(driver.getPageSource());
            LOGGER.info("starting parsing checkins");
            final List<Checkin> checkins = parseCheckins(link.get(), document, untilDateTime);
            LOGGER.info("finished parsing checkins");
            return checkins;
        } catch (final Throwable x) {
            LOGGER.info("error scraping for checkins", x);
            return ImmutableList.of();
        }
    }

    /**
     * @return true if all the needed checkins are already available, i.e. the last checkin the page is after the start time
     */
    @VisibleForTesting
    boolean needToClick(
        final Optional<ZonedDateTime> nextLastCheckin,
        final Optional<ZonedDateTime> untilDateTime
    ) {
        return untilDateTime.map(x -> nextLastCheckin.map(y -> ZonedDateTimes.isOnOrAfter(y, x)).orElse(false))
            .orElse(true);
    }

    @VisibleForTesting
    void clickNextUntilDateTime(
        final WebDriver driver,
        final Optional<ZonedDateTime> untilDateTime
    ) {
        // Click next to force the login page to appear...
        final boolean clickedNext = clickNext(driver);

        // If we didn't click next then it doesn't exist.
        if (clickedNext) {
            loginDriver.login(driver);

            Optional<ZonedDateTime> lastCheckin = Optional.empty();
            while (true) {
                clickNext(driver);
                final Optional<ZonedDateTime> nextLastCheckin = getLastCheckin(driver);

                // Keep clicking until we have the all the beer up to start time.
                final boolean stop = !needToClick(nextLastCheckin, untilDateTime)
                    || Objects.equals(lastCheckin, nextLastCheckin);

                if (stop) {
                    LOGGER.info("last checkin match, stopping clicking");
                    break;
                }
                lastCheckin = nextLastCheckin;
            }
        }
    }

    @VisibleForTesting
    WebDriver createWebDriver() {
        final ChromeOptions options = new ChromeOptions()
            .setHeadless(true);
        // Heroku build pack provides this environmental variable.
        // See https://github.com/heroku/heroku-buildpack-google-chrome
        Optional.ofNullable(System.getenv("GOOGLE_CHROME_SHIM")).ifPresent(options::setBinary);
        // BitBucket pipelines remote web driver using Docker image
        final Optional<String> remoteWebDriverUrl = Optional.ofNullable(System.getenv("REMOTE_CHROMEDRIVER_URL"));
        return remoteWebDriverUrl.map(url -> new RemoteWebDriver(createUrlSafe(url), options))
            .orElseGet(() -> new ChromeDriver(options));
    }

    private URL createUrlSafe(final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException x) {
            throw new RuntimeException(x);
        }
    }

    @VisibleForTesting
    void openPersonPage(final WebDriver driver, final UntappdLink link) {
        driver.get(UNTAPPD_URL + link.untappdName());
    }

    @VisibleForTesting
    boolean isValidPerson(final WebDriver driver) {
        try {
            return driver.findElement(ERROR_MESSAGE_PATH) == null;
        } catch (final Throwable x) {
            return true;
        }
    }

    @VisibleForTesting
    Optional<ZonedDateTime> getLastCheckin(final WebDriver driver) {
        try {
            final WebDriverWait wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.visibilityOfElementLocated(LAST_CHECKIN_PATH));
            final String rawDateTime = driver.findElement(LAST_CHECKIN_PATH).getAttribute("data-gregtime");
            return Optional.of(ZonedDateTimes.parseUntappd(rawDateTime));
        } catch (final Throwable x) {
            LOGGER.info("exception while waiting for date field", x);
            return Optional.empty();
        }
    }

    private boolean clickNext(final WebDriver driver) {
        try {
            final WebDriverWait wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.visibilityOfElementLocated(NEXT_PATH));
        } catch (final Throwable x) {
            // Next may not exist if the user hasn't checked in another beer.
            LOGGER.info("exception while waiting for next button", x);
            return false;
        }

        try {
            final WebElement next = driver.findElement(NEXT_PATH);
            final JavascriptExecutor jsDriver = (JavascriptExecutor) driver;
            jsDriver.executeScript("arguments[0].click();", next);
        } catch (final Throwable x) {
            LOGGER.info("exception while clicking next button", x);
            return false;
        }
        return true;
    }

    /**
     * Parses all checkins up to the provided date. If no date is provided then parses all checkins.
     *
     * @param document      the document to be parsed
     * @param untilDateTime date for checkins
     * @return the list of checkins respecting the datetime from the current date if provided
     */
    private List<Checkin> parseCheckins(
        final UntappdLink link,
        final Document document,
        final Optional<ZonedDateTime> untilDateTime
    ) {
        final Elements rows = ITEM_PATH.evaluate(document).getElements();
        final ImmutableList.Builder<Checkin> builder = ImmutableList.builder();

        for (final Element row : rows) {
            final Checkin checkin = parseCheckin(link, row);

            if (untilDateTime.map(x -> ZonedDateTimes.isOnOrAfter(x, checkin.timestamp())).orElse(false)) {
                break;
            }
            builder.add(checkin);
        }
        return builder.build();
    }

    private Checkin parseCheckin(final UntappdLink link, final Element row) {
        final Element beer = BEER_NAME_PATH.evaluate(row).getElements().first();
        final String beerName = beer.text();
        final String beerLink = beer.attr("href");
        final double abv = abvScraper.scrape(beerLink);
        final String brewery = BREWERY_PATH.evaluate(row).get();
        final String rawDateTime = CHECKIN_TIME_STAMP_PATH.evaluate(row).get();
        final ZonedDateTime dateTime = ZonedDateTimes.parseUntappd(rawDateTime);
        final Optional<Integer> rating = Optional.ofNullable(RATING_PATH.evaluate(row).get())
            .map(this::parseRating);
        final Style style = Style.get(STYLE_PATH.evaluate(row).get());

        return new Checkin.Builder()
            .userId(link.userId())
            .name(beerName)
            .producer(brewery)
            .timestamp(dateTime)
            .rating(rating)
            .abv(abv)
            .style(style)
            .size(style.getServingSize())
            .build();
    }

    /**
     * Ratings are of the form ".* r\d\d\d", we are avoiding using a regex to speed things up.
     */
    private int parseRating(final String rawRating) {
        final int i = rawRating.indexOf(" r");
        if (i > -1) {
            return Integer.parseInt(rawRating.substring(i + 2));
        } else {
            throw new IllegalArgumentException("invalid rating: " + rawRating);
        }
    }

    /**
     * Encapsulation to provide closing in a try/catch resource with {@link WebDriver}.
     *
     * @author spags
     */
    private static class CloseableWebDriver implements AutoCloseable {

        private final WebDriver driver;

        private CloseableWebDriver(final WebDriver driver) {
            this.driver = driver;
        }

        @Override
        public void close() {
            driver.close();
        }
    }
}