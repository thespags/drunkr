package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.ApiError;
import net.spals.drunkr.model.Person;
import net.spals.drunkr.serialization.ObjectSerializer;

/**
 * Update a {@link Person} with the given payload.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_UPDATE", keyType = CommandType.class)
class UserUpdateCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;
    private final ObjectSerializer serializer;

    @Inject
    UserUpdateCommand(
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
        final Person user = (Person) request.get("user");

        //noinspection unchecked
        final Map<String, Object> payload = (Map<String, Object>) request.get("payload");
        final Person updatedUser = serializer.patch(user, payload, Person.class);
        final boolean updated = dbService.updatePerson(updatedUser);

        if (updated) {
            return Response.status(OK)
                .entity(updatedUser)
                .build();
        }

        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.update.user.fail", updatedUser.id())
        ).asResponseBuilder().build();
    }
}
