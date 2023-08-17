package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.*;

/**
 * A ranking of the user's sphere of relationships and their BAC.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_LEADERS", keyType = CommandType.class)
class UserLeadersCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    UserLeadersCommand(
        final DatabaseService dbService
    ) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person requestingUser = (Person) request.get("user");

        // This is the user's known world, the user plus their followers plus their following.
        final Set<Person> users = ImmutableSet.<Person>builder()
            .add(requestingUser)
            .addAll(dbService.getFollowing(requestingUser))
            .build();
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        // Our jobs are run every 15 minutes, so query any calculation performed in that window.
        final ZonedDateTime before = now.minusMinutes(15);
        final Optional<ZonedDateTime> optionalNow = Optional.of(now);
        final Optional<ZonedDateTime> optionalBefore = Optional.of(before);

        final List<BacStatus> statuses = users.stream()
            .map(
                user -> new BacStatus.Builder()
                    .user(user)
                    .bac(getLatestCalculation(user, optionalBefore, optionalNow))
                    .isDrinking(dbService.getRunningJob(user, now).isPresent())
                    .build()
            )
            .sorted(Comparator.comparingDouble(BacStatus::bac))
            .collect(Collectors.toList());
        return Response.status(OK)
            .entity(statuses)
            .build();
    }

    private double getLatestCalculation(
        final Person user,
        final Optional<ZonedDateTime> before,
        final Optional<ZonedDateTime> now
    ) {
        final List<BacCalculation> bac = dbService.getBacCalculations(user, before, now);
        return bac.isEmpty() ? 0.0 : Iterables.getLast(bac).bac();
    }
}
