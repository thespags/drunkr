package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Checkin;
import net.spals.drunkr.model.Person;

/**
 * Find all {@link Checkin}'s associated to a user within the given time frame.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "CHECKIN_FIND_ALL", keyType = CommandType.class)
class CheckinFindAllCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    CheckinFindAllCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        //noinspection unchecked
        final Optional<ZonedDateTime> fromTime = (Optional<ZonedDateTime>) request.get("from");
        //noinspection unchecked
        final Optional<ZonedDateTime> toTime = (Optional<ZonedDateTime>) request.get("to");

        // For now we return all checkins.
        final List<Checkin> checkins = dbService.getCheckins(person, fromTime, toTime);

        return Response.status(OK)
            .entity(checkins)
            .build();
    }
}
