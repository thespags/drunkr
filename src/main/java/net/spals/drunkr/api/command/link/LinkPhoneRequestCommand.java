package net.spals.drunkr.api.command.link;

import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.*;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.twilio.TwilioClient;

/**
 * Provides an authentication code for the user to register a phone number.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "LINK_PHONE_REQUEST", keyType = CommandType.class)
class LinkPhoneRequestCommand implements ApiCommand {

    private final CodeGenerator generator;
    private final DatabaseService dbService;
    private final KeyStore keyStore;
    private final I18nSupport i18nSupport;
    private final TwilioClient twilioClient;

    @Inject
    LinkPhoneRequestCommand(
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
        //noinspection unchecked
        final Optional<String> region = (Optional<String>) request.get("defaultRegion");
        final Optional<String> phoneNumber = PhoneNumbers.parse(
            (String) request.get("phoneNumber"),
            region.orElse("US")
        );

        if (!phoneNumber.isPresent()) {
            return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.link.phone.request.invalid"))
                .asResponseBuilder().build();
        }

        final Optional<Person> person = dbService.getPerson(phoneNumber.get());

        if (person.isPresent()) {
            return ApiError.newError(CONFLICT, i18nSupport.getLabel("command.link.phone.request.duplicate"))
                .asResponseBuilder().build();
        }

        final String code = generator.generate();
        final String encryptedCode = keyStore.encrypt(code);

        final LinkCode linkCode = new LinkCode.Builder()
            .userId(user.id())
            .link(phoneNumber.get())
            .code(encryptedCode)
            .type(LinkType.LINK_PHONE)
            .timestamp(ZonedDateTimes.nowUTC())
            .build();

        final boolean added = dbService.insertLinkCode(linkCode);

        if (!added) {
            return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.link.phone.request.fail"))
                .asResponseBuilder().build();
        }

        final String sid = twilioClient.sendMessage(
            phoneNumber.get(),
            i18nSupport.getLabel("command.link.phone.request", code)
        );

        return Response.status(Status.OK)
            .entity(ImmutableMap.of("verificationMessageSid", sid))
            .build();
    }
}