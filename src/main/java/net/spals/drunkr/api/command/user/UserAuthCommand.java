package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.api.UsersResource;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Verifies the authentication for user.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_AUTH", keyType = CommandType.class)
class UserAuthCommand implements ApiCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersResource.class);
    private final DatabaseService dbService;
    private final KeyStore keyStore;
    private final I18nSupport i18nSupport;

    @Inject
    UserAuthCommand(
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

        final Optional<LinkCode> optionalLinkCode = dbService.getLinkCode(user, LinkType.AUTH_USER);

        if (optionalLinkCode.isPresent()) {
            final LinkCode linkCode = optionalLinkCode.get();
            final String canonicalCode = keyStore.decrypt(linkCode.code());
            final boolean isValid = Objects.equals(code, canonicalCode);
            LOGGER.info("passed in code: " + code + "==" + canonicalCode);
            LOGGER.info("linkCode: " + linkCode.id() + ":" + linkCode.userId());
            return Response.status(OK)
                .entity(ImmutableMap.of("valid", isValid))
                .build();
        }
        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.link.phone.auth.not.found")
        ).asResponseBuilder().build();
    }
}
