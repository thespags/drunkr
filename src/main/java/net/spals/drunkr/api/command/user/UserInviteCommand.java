package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.PhoneNumbers;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Invite a new {@link Person} based on their phone number.
 *
 * @author jbrock
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_INVITE", keyType = CommandType.class)
class UserInviteCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;
    private final TwilioClient twilioClient;

    @Inject
    UserInviteCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport,
        final TwilioClient twilioClient
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
        this.twilioClient = twilioClient;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Invite invite = (Invite) request.get("invite");
        final Optional<String> inviteePhoneNumber = PhoneNumbers.parse(invite.phoneNumber(), "US");
        if (!inviteePhoneNumber.isPresent()) {
            return ApiError.newError(
                NOT_FOUND,
                i18nSupport.getLabel("command.invite.invalid.phone.number")
            ).asResponseBuilder().build();
        }
        final Person inviter = (Person) request.get("user");
        if (inviter.phoneNumber().map(x -> Objects.equals(x, inviteePhoneNumber.get())).orElse(false)) {
            return ApiError.newError(
                CONFLICT,
                i18nSupport.getLabel("invite.person.same")
            ).asResponseBuilder().build();
        }
        final Optional<Person> found = dbService.getPerson(inviteePhoneNumber.get());
        if (found.isPresent()) {
            return ApiError.newError(
                CONFLICT,
                i18nSupport.getLabel("command.find.person", found.get().userName())
            ).asResponseBuilder().build();
        } else {
            final String help = i18nSupport.getLabel("command.create.help");
            twilioClient.sendMessage(
                inviteePhoneNumber.get(),
                i18nSupport.getLabel("invite.person", inviter.userName(), help)
            );
            return Response.status(OK).entity(inviteePhoneNumber.get()).build();
        }
    }
}
