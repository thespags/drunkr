package net.spals.drunkr.service.untappd;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import net.spals.drunkr.model.Checkin;
import net.spals.drunkr.model.UntappdLink;

/**
 * See {@link CheckinScraper}.
 *
 * @author spags
 */
public interface CheckinProvider {

    /**
     * @param link      contains information for finding the drunkr user's untappd information
     * @param startTime lower bound of checkins to get otherwise gets all checkins
     * @return all checkins after, inclusive, of the start time if the user is registered
     */
    @NotNull
    List<Checkin> get(Optional<UntappdLink> link, Optional<ZonedDateTime> startTime);
}
