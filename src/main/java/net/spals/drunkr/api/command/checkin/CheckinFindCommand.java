package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.model.Checkin;

/**
 * Find a specific {@link Checkin} given an id.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "CHECKIN_FIND", keyType = CommandType.class)
class CheckinFindCommand implements ApiCommand {

    @Inject
    CheckinFindCommand() {
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Checkin checkin = (Checkin) request.get("checkin");

        return Response.status(OK)
            .entity(checkin)
            .build();
    }
}
