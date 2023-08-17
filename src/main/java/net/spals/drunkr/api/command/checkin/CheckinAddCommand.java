package net.spals.drunkr.api.command.checkin;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Add a new drink {@link Checkin} for requesting {@link Person}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "CHECKIN_ADD", keyType = CommandType.class)
class CheckinAddCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    CheckinAddCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Checkin checkin = (Checkin) request.get("checkin");
        final boolean added = dbService.insertCheckin(checkin);

        if (added) {
            return Response.status(CREATED)
                .entity(checkin)
                .build();
        }

        return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.checkin.fail", checkin.name()))
            .asResponseBuilder()
            .build();
    }
}
