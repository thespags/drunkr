package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.Person;

/**
 * Locate a {@link Person} based on their Untappd username, phone number, or Messenger ID.
 *
 * @author jbrock
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_FIND", keyType = CommandType.class)
class UserFindCommand implements ApiCommand {

    @Inject
    UserFindCommand() {
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        return Response.status(OK)
            .entity(user)
            .build();
    }
}
