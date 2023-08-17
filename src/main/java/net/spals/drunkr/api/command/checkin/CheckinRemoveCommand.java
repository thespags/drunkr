package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.ApiError;
import net.spals.drunkr.model.Checkin;

/**
 * Remove a {@link Checkin} for a user.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "CHECKIN_REMOVE", keyType = CommandType.class)
class CheckinRemoveCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    CheckinRemoveCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Checkin checkin = (Checkin) request.get("checkin");

        final boolean deleted = dbService.removeCheckin(checkin);
        if (deleted) {
            return Response.status(OK)
                .entity(checkin)
                .build();
        }
        return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.checkin.remove.fail"))
            .asResponseBuilder()
            .build();
    }
}
