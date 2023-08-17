package net.spals.drunkr.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.PhoneNumbers;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.model.JobOptions.Builder;

/**
 * See {@link TextBasedParser}.
 *
 * @author spags
 */
@AutoBindSingleton(baseClass = TextBasedParser.class)
class CommonTextBasedParser implements TextBasedParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTextBasedParser.class);
    private final Map<CommandType, ApiCommand> commands;
    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;
    private final Function<Response, String> genericResponse;

    @Inject
    CommonTextBasedParser(
        final Map<CommandType, ApiCommand> commands,
        final DatabaseService dbService,
        final I18nSupport i18nSupport
    ) {
        this.commands = commands;
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
        genericResponse = response -> i18nSupport.getLabel("command.processed");
    }

    @Override
    public String parse(final Source source, final String userId, final String body) {
        try {
            // split to list is never empty
            final List<String> tokens = Splitter.on(" ").splitToList(Strings.nullToEmpty(body).trim());
            CommandType commandType = CommandType.INVALID;
            final Map<String, Object> request = new HashedMap<>();
            final String command = tokens.get(0).toUpperCase();

            final Optional<Person> user = dbService.getPerson(userId);
            if (user.isPresent()) {
                request.put("user", user.get());
                switch (command) {
                case "CREATE":
                    return i18nSupport.getLabel("command.create.person.existing");
                case "CHECK":
                    return processCheck(user.get());
                case "BEGIN":
                    // Subtract 5 minutes if you checked in before you started...
                    final ZonedDateTime now = ZonedDateTimes.nowUTC();
                    final JobOptions jobOptions = new Builder()
                        .userId(user.get().id())
                        .startTime(now.minusMinutes(5))
                        .source(source)
                        .build();
                    request.put("jobOptions", jobOptions);
                    return run(CommandType.JOB_START, request, genericResponse);
                case "FINISH":
                    return processStop(user.get(), request);
                case "CHECKIN":
                    // Text based only supports adding checkins.
                    commandType = addCheckin(user.get(), tokens, request);
                    break;
                case "DOC":
                    // Doc isn't apart of the API, but helpful hints for text.
                    return parseDoc(tokens);
                case "FOLLOW":
                    return processFollow(tokens, request);
                case "FOLLOWER":
                    return processFollower(tokens, request);
                }
            } else {
                // Only command possible if user does not exist is to create one...
                switch (command) {
                case "CREATE":
                    if (tokens.size() != 4) {
                        commandType = CommandType.INVALID;
                        break;
                    }
                    final String name = tokens.get(1);
                    final Gender gender = Gender.valueOf(tokens.get(2).toUpperCase());
                    final double weight = Double.valueOf(tokens.get(3));
                    final Optional<String> phoneNumber = PhoneNumbers.parse(userId, "US");
                    final Person.Builder person = new Person.Builder()
                        .userName(name)
                        .gender(gender)
                        .weight(weight);
                    // This gets cleaned up if we convert to java 9...
                    if (phoneNumber.isPresent()) {
                        person.phoneNumber(phoneNumber.get());
                    } else {
                        person.messengerId(userId);
                    }
                    request.put("person", person.build());
                    commandType = CommandType.USER_ADD;
                    break;
                default:
                    return i18nSupport.getLabel("command.doc.create");
                }
            }
            return run(commandType, request, genericResponse);
        } catch (final Throwable x) {
            LOGGER.info("Exception during text based command run", x);
            return x.getMessage();
        }
    }

    private String run(
        final CommandType commandType,
        final Map<String, Object> request,
        final Function<Response, String> responder
    ) {
        final Response response = commands.get(commandType).run(request);
        final Status status = Status.fromStatusCode(response.getStatus());
        if (status.getFamily() == Family.SUCCESSFUL) {
            return responder.apply(response);
        } else {
            // All non successful responses should be of ApiError.
            //noinspection unchecked
            return ((Map<String, Object>) response.getEntity())
                .get("message")
                .toString();
        }
    }

    /**
     * We grab the last calculation so this isn't going through the internal API.
     */
    private String processCheck(final Person user) {
        final List<BacCalculation> calculations = dbService.getBacCalculations(
            user,
            Optional.empty(),
            Optional.empty()
        );
        if (calculations.isEmpty()) {
            return i18nSupport.getLabel("command.check.none");
        }
        final BacCalculation calculation = Iterables.getLast(calculations);
        return i18nSupport.getLabel("command.check", user.userName(), calculation.bac(), calculation.timestamp());
    }

    /**
     * Find the first non stop job, as we can only have one job in progress, shut it down.
     */
    private String processStop(final Person user, final Map<String, Object> request) {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final JobOptions job = dbService.getJobs(user).stream()
            .filter(x -> x.stopTime().map(stopTime -> ZonedDateTimes.isOnOrAfter(stopTime, now)).orElse(true))
            .findFirst()
            .orElse(null);

        request.put("job", job);
        return run(CommandType.JOB_STOP, request, genericResponse);
    }

    private CommandType addCheckin(
        final Person user,
        final List<String> tokens,
        final Map<String, Object> request
    ) {
        if (tokens.size() != 2) {
            return CommandType.INVALID;
        }

        final String subCommand = tokens.get(1).toUpperCase();
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final Checkin.Builder builder = new Checkin.Builder()
            .userId(user.id())
            .timestamp(now)
            .style(Style.NONE);

        switch (subCommand) {
        case "SHOT":
            builder.name("Shot")
                .abv(.40)
                .size(1.5);
            request.put("checkin", builder.build());
            return CommandType.CHECKIN_ADD;
        case "WINE":
            builder.name("Wine")
                .abv(.12)
                .size(5.0);
            request.put("checkin", builder.build());
            return CommandType.CHECKIN_ADD;
        case "BEER":
            builder.name("Beer")
                .abv(.05)
                .size(12.0);
            request.put("checkin", builder.build());
            return CommandType.CHECKIN_ADD;
        }
        return CommandType.INVALID;
    }

    private String processFollow(final List<String> tokens, final Map<String, Object> request) {
        if (tokens.size() >= 2) {
            final String subCommand = tokens.get(1).toUpperCase();
            switch (subCommand) {
            case "ADD":
                if (tokens.size() != 3) {
                    break;
                }
                request.put("targetUserId", tokens.get(2));
                return run(CommandType.FOLLOWING_ADD, request, genericResponse);
            case "REMOVE":
                if (tokens.size() != 3) {
                    break;
                }
                final String targetUserId = tokens.get(2);
                final Optional<Person> targetUser = dbService.getPerson(targetUserId);
                if (targetUser.isPresent()) {
                    request.put("targetUser", targetUser.get());
                    return run(CommandType.FOLLOWING_REMOVE, request, genericResponse);
                }
                return i18nSupport.getLabel("invalid.user", targetUserId);
            case "LIST":
                if (tokens.size() != 2) {
                    break;
                }
                //noinspection unchecked
                final String following = run(
                    CommandType.FOLLOWING_LIST,
                    request,
                    response -> ((Set<Person>) response.getEntity()).stream()
                        .map(Person::userName)
                        .collect(Collectors.joining("\n"))
                );
                return i18nSupport.getLabel("command.follow.list", following);
            }
        }
        return run(CommandType.INVALID, request, genericResponse);
    }

    private String processFollower(final List<String> tokens, final Map<String, Object> request) {
        if (tokens.size() >= 2) {
            final String subCommand = tokens.get(1).toUpperCase();
            switch (subCommand) {
            case "INVITE":
                if (tokens.size() != 3) {
                    break;
                }
                request.put("targetUserId", tokens.get(2));
                return run(CommandType.FOLLOWER_INVITE, request, genericResponse);
            case "REMOVE":
                if (tokens.size() != 3) {
                    break;
                }
                final String targetUserId = tokens.get(2);
                final Optional<Person> targetUser = dbService.getPerson(targetUserId);
                if (targetUser.isPresent()) {
                    request.put("targetUser", targetUser.get());
                    return run(CommandType.FOLLOWER_REMOVE, request, genericResponse);
                }
                return i18nSupport.getLabel("invalid.user", targetUserId);
            case "LIST":
                if (tokens.size() != 2) {
                    break;
                }
                //noinspection unchecked
                final String followers = run(
                    CommandType.FOLLOWER_LIST,
                    request,
                    response -> ((Set<Person>) response.getEntity()).stream()
                        .map(Person::userName)
                        .collect(Collectors.joining("\n"))
                );
                return i18nSupport.getLabel("command.follower.list", followers);
            }
        }
        return run(CommandType.INVALID, request, genericResponse);
    }

    private String parseDoc(final List<String> tokens) {
        if (tokens.size() < 2) {
            return i18nSupport.getLabel("command.doc.all");
        }
        final String command = tokens.get(1).toUpperCase();
        switch (command) {
        case "BEGIN":
            return i18nSupport.getLabel("command.doc.begin");
        case "CREATE":
            return i18nSupport.getLabel("command.doc.create");
        case "CHECK":
            return i18nSupport.getLabel("command.doc.check");
        case "CHECKIN":
            return i18nSupport.getLabel("command.doc.checkin");
        case "DOC":
            return i18nSupport.getLabel("command.doc.doc");
        case "FINISH":
            return i18nSupport.getLabel("command.doc.finish");
        case "FOLLOW": {
            if (tokens.size() != 3) {
                return i18nSupport.getLabel("command.doc.follow");
            }
            final String subCommand = tokens.get(2).toUpperCase();
            switch (subCommand) {
            case "ADD":
                return i18nSupport.getLabel("command.doc.follow_add");
            case "LIST":
                return i18nSupport.getLabel("command.doc.follow_list");
            case "REMOVE":
                return i18nSupport.getLabel("command.doc.follow_remove");
            }
        }
        case "FOLLOWER": {
            if (tokens.size() != 3) {
                return i18nSupport.getLabel("command.doc.follower");
            }
            final String subCommand = tokens.get(2).toUpperCase();
            switch (subCommand) {
            case "INVITE":
                return i18nSupport.getLabel("command.doc.follower_invite");
            case "LIST":
                return i18nSupport.getLabel("command.doc.follower_list");
            case "REMOVE":
                return i18nSupport.getLabel("command.doc.follower_remove");
            }
        }
        }
        return i18nSupport.getLabel("command.doc.invalid");
    }
}