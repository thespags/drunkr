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
 * List all users who you follower.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "FOLLOWING_LIST", keyType = CommandType.class)
class FollowingListCommand implements ApiCommand {

    private final DatabaseService dbService;

    @Inject
    FollowingListCommand(final DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person follower = (Person) request.get("user");
        final Set<Person> follows = dbService.getFollowing(follower);

        return Response.status(OK)
            .entity(follows)
            .build();
    }
}
