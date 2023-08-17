package net.spals.drunkr.api.command.link;

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
import net.spals.drunkr.model.UntappdLink.Builder;

/**
 * Creates a {@link UntappdLink} with an Untappd username, that can be authorized through our web portal.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "LINK_UNTAPPD", keyType = CommandType.class)
class LinkUntappdCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    LinkUntappdCommand(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        final String userName = (String) request.get("userName");
        final UntappdLink link = new Builder()
            .userId(user.id())
            .untappdName(userName)
            .build();
        final boolean added = dbService.insertUntappdLink(link);

        if (added) {
            return Response.status(OK)
                .entity(link)
                .build();
        }

        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.link.untappd.fail", user.userName(), userName)
        ).asResponseBuilder().build();
    }
}
