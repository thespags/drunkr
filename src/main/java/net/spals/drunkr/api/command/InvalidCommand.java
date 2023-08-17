package net.spals.drunkr.api.command;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.ApiError;

/**
 * Invalid command that should only be processed by text based services.
 * Otherwise invalid REST calls should be handled by jersey.
 *
 * @author jbrock
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "INVALID", keyType = CommandType.class)
class InvalidCommand implements ApiCommand {

    private final I18nSupport i18nSupport;

    @Inject
    InvalidCommand(final I18nSupport i18nSupport) {
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        return ApiError.newError(INTERNAL_SERVER_ERROR, i18nSupport.getLabel("command.invalid"))
            .asResponseBuilder()
            .build();
    }
}
