package net.spals.drunkr.i18n;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.*;

import com.google.inject.Inject;

import com.netflix.governator.annotations.Configuration;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * Internationalization for when we go big.
 *
 * @author spags
 */
@AutoBindSingleton(baseClass = I18nSupport.class)
class StandardI18nSupport implements I18nSupport {

    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("i18n.language")
    private String language = "en";
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("i18n.country")
    private String country = "US";
    private ResourceBundle resourceBundle;

    @Inject
    StandardI18nSupport() {
    }

    @PostConstruct
    void createResourceBundle() {
        final Locale locale = new Locale(language, country);
        this.resourceBundle = ResourceBundle.getBundle("i18n/messages", locale);
    }

    @Override
    public String getLabel(final String key) {
        try {
            return resourceBundle.getString(key);
        } catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    @Override
    public String getLabel(final String key, final Object... values) {
        try {
            return MessageFormat.format(getLabel(key), values);
        } catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}