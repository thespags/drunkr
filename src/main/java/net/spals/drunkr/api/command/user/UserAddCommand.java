package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.ApiError;
import net.spals.drunkr.model.Person;

/**
 * Create and set up a new Drunkr {@link Person}.
 *
 * @author jbrock
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_ADD", keyType = CommandType.class)
class UserAddCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    UserAddCommand(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");

        final Optional<Person> fromUserName = dbService.getPerson(user.userName());
        if (fromUserName.isPresent()) {
            return ApiError.newError(
                BAD_REQUEST,
                i18nSupport.getLabel("command.create.person.existing.username", user.userName())
            ).asResponseBuilder().build();
        }
        final Optional<Person> fromPhone = user.phoneNumber().flatMap(this::getPerson);
        if (fromPhone.isPresent()) {
            return ApiError.newError(
                BAD_REQUEST,
                i18nSupport.getLabel("command.create.person.existing")
            ).asResponseBuilder().build();
        }
        final Optional<Person> fromMessenger = user.messengerId().flatMap(this::getPerson);
        if (fromMessenger.isPresent()) {
            return ApiError.newError(
                BAD_REQUEST,
                i18nSupport.getLabel("command.create.person.existing")
            ).asResponseBuilder().build();
        }
        final boolean added = dbService.insertPerson(user);
        if (added) {
            return Response.status(CREATED)
                .entity(user)
                .build();
        } else {
            return ApiError.newError(
                INTERNAL_SERVER_ERROR,
                i18nSupport.getLabel("command.create.person.fail")
            ).asResponseBuilder().build();
        }
    }

    private Optional<Person> getPerson(final String personQuery) {
        return dbService.getPerson(personQuery);
    }
}
