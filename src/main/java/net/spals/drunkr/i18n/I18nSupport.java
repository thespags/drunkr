package net.spals.drunkr.i18n;

/**
 * @author spags
 */
public interface I18nSupport {

    String getLabel(String key);

    String getLabel(String key, Object... values);
}
