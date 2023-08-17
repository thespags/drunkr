package net.spals.drunkr.api.command.user;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.CodeGenerator;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Sends an auth code to the user for authenticating the user.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "USER_AUTH_REQUEST", keyType = CommandType.class)
class UserAuthRequestCommand implements ApiCommand {

    private final CodeGenerator generator;
    private final DatabaseService dbService;
    private final KeyStore keyStore;
    private final I18nSupport i18nSupport;
    private final TwilioClient twilioClient;

    @Inject
    UserAuthRequestCommand(
        final CodeGenerator generator,
        final DatabaseService dbService,
        final KeyStore keyStore,
        final I18nSupport i18nSupport,
        final TwilioClient twilioClient
    ) {
        this.generator = generator;
        this.dbService = dbService;
        this.keyStore = keyStore;
        this.i18nSupport = i18nSupport;
        this.twilioClient = twilioClient;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");

        if (!user.phoneNumber().isPresent()) {
            return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.link.phone.request.invalid"))
                .asResponseBuilder().build();
        }

        final String phoneNumber = user.phoneNumber().get();
        final String code = generator.generate();
        final String encryptedCode = keyStore.encrypt(code);

        final LinkCode linkCode = new LinkCode.Builder()
            .userId(user.id())
            .link("")
            .code(encryptedCode)
            .type(LinkType.AUTH_USER)
            .timestamp(ZonedDateTimes.nowUTC())
            .build();

        final boolean added = dbService.insertLinkCode(linkCode);

        if (!added) {
            return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.link.phone.request.fail"))
                .asResponseBuilder().build();
        }

        final String sid = twilioClient.sendMessage(
            phoneNumber,
            i18nSupport.getLabel("command.link.phone.request", code)
        );

        return Response.status(Status.OK)
            .entity(ImmutableMap.of("verificationMessageSid", sid))
            .build();
    }
}
