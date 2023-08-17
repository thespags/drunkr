package net.spals.drunkr.api.command.link;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Given a code from the user validates if the phone number is correct.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "LINK_PHONE_AUTH", keyType = CommandType.class)
class LinkPhoneAuthCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final KeyStore keyStore;
    private final I18nSupport i18nSupport;

    @Inject
    LinkPhoneAuthCommand(
        final DatabaseService dbService,
        final KeyStore keyStore,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.keyStore = keyStore;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        final String code = (String) request.get("code");

        final Optional<LinkCode> optionalLinkCode = dbService.getLinkCode(user, LinkType.LINK_PHONE);

        if (optionalLinkCode.isPresent()) {
            final LinkCode linkCode = optionalLinkCode.get();
            final String canonicalCode = keyStore.decrypt(linkCode.code());

            if (Objects.equals(code, canonicalCode)) {
                final Person updatedUser = new Person.Builder()
                    .mergeFrom(user)
                    .phoneNumber(linkCode.link())
                    .build();
                final boolean updated = dbService.updatePerson(updatedUser);
                if (updated) {
                    return Response.status(OK)
                        .entity(updatedUser)
                        .build();
                }
                return ApiError.newError(
                    INTERNAL_SERVER_ERROR,
                    i18nSupport.getLabel("command.update.person.fail", user.userName())
                ).asResponseBuilder().build();
            }
            return ApiError.newError(
                INTERNAL_SERVER_ERROR,
                i18nSupport.getLabel("command.link.phone.auth.invalid")
            ).asResponseBuilder().build();
        }
        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.link.phone.auth.not.found")
        ).asResponseBuilder().build();
    }
}
