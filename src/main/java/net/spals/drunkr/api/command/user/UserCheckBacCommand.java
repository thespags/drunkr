package net.spals.drunkr.api.command.user;

import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.BacCalculation;
import net.spals.drunkr.model.Person;

/**
 * Gets all BAC of a {@link Person} within the provided time range.
 *
 * @author jbrock
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_CHECK_BAC", keyType = CommandType.class)
class UserCheckBacCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    UserCheckBacCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        //noinspection unchecked
        final Optional<ZonedDateTime> fromTime = (Optional<ZonedDateTime>) request.get("from");
        //noinspection unchecked
        final Optional<ZonedDateTime> toTime = (Optional<ZonedDateTime>) request.get("to");

        final List<BacCalculation> calculations = dbService.getBacCalculations(user, fromTime, toTime);

        return Response.ok()
            .entity(calculations)
            .build();
    }
}
