package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Remove a user following you.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "FOLLOWER_REMOVE", keyType = CommandType.class)
class FollowerRemoveCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    FollowerRemoveCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        final Person follower = (Person) request.get("targetUser");

        final Set<Person> following = dbService.getFollowers(user);
        if (!following.contains(follower)) {
            return ApiError.newError(
                NOT_FOUND,
                i18nSupport.getLabel("command.follower.remove.not.exists", follower.userName())
            ).asResponseBuilder().build();
        }

        final boolean removed = dbService.removeFollower(user, follower);
        if (removed) {
            final String inviteMessage = i18nSupport.getLabel(
                "command.follower.remove.message",
                user.userName()
            );
            final Notification notification = new Notification.Builder()
                .sourceUserId(user.id())
                .userId(follower.id())
                .message(inviteMessage)
                .timestamp(ZonedDateTimes.nowUTC())
                .build();
            dbService.insertNotification(notification);

            return Response.status(OK)
                .entity(follower)
                .build();
        }

        return ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.follower.remove.fail", follower.userName())
        ).asResponseBuilder().build();
    }
}
