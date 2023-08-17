package net.spals.drunkr.model;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.inferred.freebuilder.FreeBuilder;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.field.*;
import net.spals.drunkr.service.DrunkrJob;

/**
 * A option POJO for running a {@link DrunkrJob}.
 *
 * @author jbrock
 */
@FreeBuilder
@JsonDeserialize(builder = JobOptions.Builder.class)
public interface JobOptions extends HasId, HasLastModified {

    ObjectId userId();

    /**
     * The time the job is has started or will start.
     */
    ZonedDateTime startTime();

    /**
     * The time the job has finished as determined by being sober.
     */
    Optional<ZonedDateTime> stopTime();

    Source source();

    /**
     * Represents the number of seconds between iterations of the job.
     */
    Optional<Long> period();

    class Builder extends JobOptions_Builder implements HasIdBuilder<Builder> {

        private final ZonedDateTime now;
        public Builder() {
            now = ZonedDateTimes.nowUTC();
            id(new ObjectId());
            // Set the startTime and last modified as now.
            startTime(now);
            lastModified(now);
        }

        public JobOptions build() {
            // If start time does NOT use the default, and last time is still using the default.
            // Then last time is changed to reflect the start time.
            if (Objects.equals(lastModified(), now) && !Objects.equals(startTime(), now)) {
                lastModified(startTime());
            }
            // If start time is using the default, and last time is NOT using the default
            // Then this behavior is undefined.
            return super.build();
        }
    }
}
