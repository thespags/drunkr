package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.PhoneNumbers;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;

/**
 * Invite a user to follow you.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "FOLLOWER_INVITE", keyType = CommandType.class)
class FollowerInviteCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    FollowerInviteCommand(
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person user = (Person) request.get("user");
        final String followerId = PhoneNumbers.tryParse(request.get("targetUserId").toString());
        final Optional<Person> optionalFollower = dbService.getPerson(followerId);

        if (optionalFollower.isPresent()) {
            final Person follower = optionalFollower.get();
            final Set<Person> followers = dbService.getFollowers(user);
            if (followers.contains(follower)) {
                return ApiError.newError(
                    CONFLICT,
                    i18nSupport.getLabel("command.follower.invite.exists", follower.userName())
                ).asResponseBuilder().build();
            }

            if (Objects.equals(user, follower)) {
                return ApiError.newError(
                    CONFLICT,
                    i18nSupport.getLabel("command.follower.invite.self")
                ).asResponseBuilder().build();
            }

            // Publish a notification the user was asked to follow someone.
            final String inviteMessage = i18nSupport.getLabel(
                "command.follower.invite.message",
                user.userName()
            );
            final Notification notification = new Notification.Builder()
                .sourceUserId(user.id())
                .userId(follower.id())
                .message(inviteMessage)
                .timestamp(ZonedDateTimes.nowUTC())
                .build();
            dbService.insertNotification(notification);

            return Response.status(CREATED)
                .entity(follower)
                .build();
        }
        return ApiError.newError(NOT_FOUND, i18nSupport.getLabel("invalid.user", followerId))
            .asResponseBuilder()
            .build();
    }
}
