package net.spals.drunkr.api.command.follow;

import static javax.ws.rs.core.Response.Status.*;

import javax.ws.rs.core.Response;
import java.util.*;

import com.google.common.collect.ImmutableMap;
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
 * Add a user to your list of following.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ApiCommand.class, key = "FOLLOWING_ADD", keyType = CommandType.class)
class FollowingAddCommand implements ApiCommand {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    FollowingAddCommand(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Response run(final Map<String, Object> request) {
        final Person follower = (Person) request.get("user");
        final String toFollowId = PhoneNumbers.tryParse(request.get("targetUserId").toString());
        final Optional<Person> optionalToFollow = dbService.getPerson(toFollowId);

        if (optionalToFollow.isPresent()) {
            final Person toFollow = optionalToFollow.get();
            final Set<Person> following = dbService.getFollowing(follower);
            if (following.contains(toFollow)) {
                return ApiError.newError(
                    CONFLICT,
                    i18nSupport.getLabel("command.follow.add.exists", toFollow.userName())
                ).asResponseBuilder().build();
            }

            if (Objects.equals(follower, toFollow)) {
                return ApiError.newError(
                    CONFLICT,
                    i18nSupport.getLabel("command.follow.add.self")
                ).asResponseBuilder().build();
            }

            final boolean added = dbService.addFollower(toFollow, follower);
            if (added) {
                final String inviteMessage = i18nSupport.getLabel(
                    "command.follow.add.message",
                    follower.userName()
                );
                final Notification notification = new Notification.Builder()
                    .sourceUserId(follower.id())
                    .userId(toFollow.id())
                    .message(inviteMessage)
                    .timestamp(ZonedDateTimes.nowUTC())
                    .build();
                dbService.insertNotification(notification);

                return Response.status(CREATED)
                    .entity(ImmutableMap.of("followee", toFollow, "follower", follower))
                    .build();
            }

            return ApiError.newError(
                INTERNAL_SERVER_ERROR,
                i18nSupport.getLabel("command.follow.add.fail", toFollow.userName())
            ).asResponseBuilder().build();
        }
        return ApiError.newError(NOT_FOUND, i18nSupport.getLabel("invalid.user", toFollowId))
            .asResponseBuilder()
            .build();
    }
}