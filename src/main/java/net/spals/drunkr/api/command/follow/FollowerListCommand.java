package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.model.Person;

/**
 * List all the users following you.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "FOLLOWER_LIST", keyType = CommandType.class)
class FollowerListCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    FollowerListCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        final Set<Person> followers = dbService.getFollowers(user);

        return Response.status(OK)
            .entity(followers)
            .build();
    }
}
