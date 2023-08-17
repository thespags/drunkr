package net.spals.drunkr.api.command;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.ApiError;
import net.spals.drunkr.model.UntappdLink;

/**
 * Authorization for recording a {@link UntappdLink#accessToken()}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "UNTAPPD_AUTH", keyType = CommandType.class)
public class UntappdAuthCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    UntappdAuthCommand(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final String untappdUserName = request.get("untappdUserName").toString();
        final Optional<UntappdLink> link = dbService.getUntappdLink(untappdUserName);

        if (link.isPresent()) {
            final UntappdLink untappdLink = link.get();
            final String accessToken = request.get("accessToken").toString();
            final boolean linked = dbService.updateLinkAccessToken(untappdLink, accessToken);

            if (linked) {
                return Response.status(OK)
                    .entity(link.get())
                    .build();
            }
            return ApiError.newError(
                INTERNAL_SERVER_ERROR,
                i18nSupport.getLabel("command.link.untappd.auth.fail", untappdUserName)
            ).asResponseBuilder().build();
        } else {
            return ApiError.newError(
                NOT_FOUND,
                i18nSupport.getLabel("command.link.untappd.auth.missing", untappdUserName)
            ).asResponseBuilder().build();
        }
    }
}
