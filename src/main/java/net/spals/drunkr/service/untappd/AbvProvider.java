package net.spals.drunkr.service.untappd;

/**
 * See {@link AbvScraper}.
 *
 * @author spags
 */
interface AbvProvider {

    double scrape(String link);
}
