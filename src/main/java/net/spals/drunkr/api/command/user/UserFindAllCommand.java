package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Person;

/**
 * Locate a {@link Person} based on their Untappd username, phone number, or Messenger ID.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_FIND_ALL", keyType = CommandType.class)
class UserFindAllCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    UserFindAllCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final List<Person> persons = dbService.allPersons();

        return Response.status(OK)
            .entity(persons)
            .build();
    }
}
