package net.spals.drunkr.api;

import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.model.Source;

/**
 * Limited capabilities of {@link ApiCommand} for {@link TwilioResource} and {@link MessengerResource} entry points
 * which are purely text based.
 *
 * @author spags
 */
interface TextBasedParser {

    String parse(final Source source, String userId, String body);
}
