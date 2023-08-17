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
import net.spals.drunkr.model.*;
import net.spals.drunkr.serialization.ObjectSerializer;

/**
 * Update an existing {@link Checkin} for the requesting {@link Person}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "CHECKIN_UPDATE", keyType = CommandType.class)
class CheckinUpdateCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;
    private final ObjectSerializer serializer;

    @Inject
    CheckinUpdateCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport,
        final ObjectSerializer serializer
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
        this.serializer = serializer;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Checkin checkin = (Checkin) request.get("checkin");

        //noinspection unchecked
        final Map<String, Object> payload = (Map<String, Object>) request.get("payload");
        final Checkin updatedCheckin = serializer.patch(checkin, payload, Checkin.class);
        final boolean updated = dbService.updateCheckin(updatedCheckin);

        if (updated) {
            return Response.status(OK)
                .entity(updatedCheckin)
                .build();
        }

        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.checkin.update.fail", updatedCheckin.id())
        ).asResponseBuilder().build();
    }
}
