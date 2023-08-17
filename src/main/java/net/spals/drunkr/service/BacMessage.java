package net.spals.drunkr.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.spals.drunkr.i18n.I18nSupport;

/**
 * Given the blood alcohol content, will provide a message of affects.
 *
 * @author spags
 */
class BacMessage {

    private final I18nSupport i18nSupport;

    @Inject
    BacMessage(final I18nSupport i18nSupport) {
        this.i18nSupport = i18nSupport;
    }

    String get(final double bac) {
        Preconditions.checkArgument(bac >= .00, "Invalid bac, must be non negative.");
        if (bac == .00) {
            return i18nSupport.getLabel("level_sober");
        } else if (bac < .02) {
            return i18nSupport.getLabel("level_basically_sober");
        } else if (bac < .05) {
            return i18nSupport.getLabel("level_lightheaded");
        } else if (bac < .08) {
            return i18nSupport.getLabel("level_buzzed");
        } else if (bac < .11) {
            return i18nSupport.getLabel("level_legally_impaired");
        } else if (bac < .16) {
            return i18nSupport.getLabel("level_drunk");
        } else if (bac < .20) {
            return i18nSupport.getLabel("level_very_drunk");
        } else if (bac < .25) {
            return i18nSupport.getLabel("level_dazed_and_confused");
        } else if (bac < .31) {
            return i18nSupport.getLabel("level_stupor");
        } else {
            return i18nSupport.getLabel("level_coma");
        }
    }
}