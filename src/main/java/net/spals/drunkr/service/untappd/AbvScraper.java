package net.spals.drunkr.service.untappd;

import java.util.Objects;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.xsoup.XPathEvaluator;
import us.codecraft.xsoup.Xsoup;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * Scrapes a beer page looking for the ABV of the beer as checkins do not contain ABV.
 *
 * @author spags
 */
@AutoBindSingleton(baseClass = AbvProvider.class)
class AbvScraper implements AbvProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbvScraper.class);
    private static final String UNTAPPD_URL = "https://untappd.com/";
    private static final XPathEvaluator ABV_PATH = Xsoup.compile("//div[1]/div[1]/div[1]/div/div[2]/p[@class=\"abv\"]");
    private static final String NO_ABV = "No ABV";

    @Inject
    AbvScraper() {
    }

    /**
     * Scrapes a beer's abv off of it's Untappd page.
     *
     * @param link the beer url
     * @return abv of the beer
     */
    @Override
    public double scrape(final String link) {
        if (Strings.isNullOrEmpty(link)) {
            return 0.0;
        }

        final String fullUrl = UNTAPPD_URL + link;
        try {
            final Document document = Jsoup.connect(fullUrl).get();
            final String abv = ABV_PATH.evaluate(document).getElements().get(0).text();
            return Objects.equals(abv, NO_ABV) ? 0.0 : parseAbv(abv);
        } catch (final Throwable x) {
            LOGGER.info("failed to connect to beer page: " + fullUrl, x);
            return 0.0;
        }
    }

    /**
     * Assumes untappd ABV's are of the format 'd% ABV', 'd.d% ABV', 'd.dd% ABV' etc.
     */
    private double parseAbv(final String abv) {
        final int percentIndex = abv.indexOf('%');
        if (percentIndex <= 0) {
            throw new IllegalArgumentException("Invalid ABV format: " + abv);
        }
        return Double.parseDouble(abv.substring(0, percentIndex)) / 100;
    }
}
