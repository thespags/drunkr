package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Remove all checkins for the given user.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "CHECKIN_REMOVE_ALL", keyType = CommandType.class)
class CheckinRemoveAllCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    CheckinRemoveAllCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person person = (Person) request.get("user");
        final List<Checkin> checkins = dbService.getCheckins(person, Optional.empty(), Optional.empty());
        final boolean deleted = dbService.removeCheckins(person);

        if (deleted) {
            return Response.status(OK)
                .entity(checkins)
                .build();
        }
        return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.checkin.remove.all.fail"))
            .asResponseBuilder()
            .build();
    }
}
