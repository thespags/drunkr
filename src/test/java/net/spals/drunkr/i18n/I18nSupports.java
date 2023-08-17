package net.spals.drunkr.i18n;

/**
 * Factory class for creating {@link I18nSupport}. The intention is to avoid leaking implementations such as {@link StandardI18nSupport}.
 *
 * @author spags
 */
public class I18nSupports {

    private I18nSupports() {
    }

    public static I18nSupport getEnglish() {
        final StandardI18nSupport i18nSupport = new StandardI18nSupport();
        i18nSupport.createResourceBundle();
        return i18nSupport;
    }
}
