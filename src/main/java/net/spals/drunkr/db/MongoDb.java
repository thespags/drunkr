package net.spals.drunkr.db;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.*;
import com.google.inject.Inject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;
import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.model.*;

/**
 * Mongo DB implementation of DatabaseService. Singleton service is injected with auto-bind.
 *
 * @author jbrock
 */
@AutoBindSingleton(baseClass = DatabaseService.class)
class MongoDb implements DatabaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDb.class);
    private static final String ID = "_id";
    private static final String FOLLOWING_ID = "userId";
    private static final String FOLLOWER_ID = "followerId";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String MESSENGER_ID = "messengerId";
    private static final String UNTAPPD_NAME = "untappdName";
    private static final String PHONE_INDEX_NAME_CURRENT = "phone_partial_1";
    private static final String MESSENGER_INDEX_NAME_CURRENT = "messenger_partial_1";
    private static final String START_TIME = "startTime";
    private static final String STOP_TIME = "stopTime";
    private static final String LAST_MODIFIED = "lastModified";
    private static final String TIMESTAMP = "timestamp";
    private static final Bson ORDER_BY_TIMESTAMP_ASC = Sorts.ascending(TIMESTAMP);
    private static final Bson ORDER_BY_TIMESTAMP_DESC = Sorts.descending(TIMESTAMP);
    private static final Bson ORDER_BY_START_TIME_ASC = Sorts.ascending(START_TIME);
    private static final Bson ORDER_BY_START_TIME_DESC = Sorts.descending(START_TIME);
    private final KeyStore keyStore;
    private final MongoCollection<Person> users;
    private final MongoCollection<Follower> followers;
    private final MongoCollection<Checkin> checkins;
    private final MongoCollection<Notification> notifications;
    private final MongoCollection<BacCalculation> bacCalculations;
    private final MongoCollection<UntappdLink> untappdLinks;
    private final MongoCollection<JobOptions> jobs;
    private final MongoCollection<LinkCode> linkCodes;

    @Inject
    MongoDb(final MongoDatabase database, final KeyStore keyStore) {
        this.keyStore = keyStore;
        users = database.getCollection("persons", Person.class);
        followers = database.getCollection("followers", Follower.class);
        checkins = database.getCollection("checkins", Checkin.class);
        notifications = database.getCollection("notifications", Notification.class);
        bacCalculations = database.getCollection("bacCalculations", BacCalculation.class);
        untappdLinks = database.getCollection("untappdLinks", UntappdLink.class);
        jobs = database.getCollection("jobs", JobOptions.class);
        linkCodes = database.getCollection("linkCodes", LinkCode.class);
    }

    @PostConstruct
    @VisibleForTesting
    void buildIndexes() {
        final IndexOptions userName = new IndexOptions()
            .unique(true)
            .collation(
                Collation.builder()
                    .collationStrength(CollationStrength.SECONDARY)
                    .locale("en")
                    .build()
            );
        users.createIndex(Indexes.ascending(USER_NAME), userName);
        final IndexOptions phoneNumber = new IndexOptions()
            .unique(true)
            .partialFilterExpression(Filters.exists(PHONE_INDEX_NAME_CURRENT));
        users.createIndex(Indexes.ascending(PHONE_NUMBER), phoneNumber);
        final IndexOptions messengerId = new IndexOptions()
            .unique(true)
            .partialFilterExpression(Filters.exists(MESSENGER_INDEX_NAME_CURRENT));
        users.createIndex(Indexes.ascending(MESSENGER_ID), messengerId);

        followers.createIndex(Indexes.ascending(FOLLOWER_ID));
        followers.createIndex(Indexes.ascending(FOLLOWING_ID));
        final IndexOptions followersIndex = new IndexOptions()
            .unique(true);
        followers.createIndex(
            Indexes.compoundIndex(Indexes.ascending(FOLLOWER_ID), Indexes.ascending(FOLLOWING_ID)),
            followersIndex
        );

        checkins.createIndex(Indexes.ascending(USER_ID));

        notifications.createIndex(Indexes.ascending(USER_ID));
        notifications.createIndex(Indexes.ascending("pushed"));

        bacCalculations.createIndex(Indexes.ascending(USER_ID));

        untappdLinks.createIndex(Indexes.ascending(USER_ID));
        untappdLinks.createIndex(Indexes.ascending(UNTAPPD_NAME));

        jobs.createIndex(Indexes.ascending(USER_ID));

        linkCodes.createIndex(Indexes.ascending(USER_ID));
        linkCodes.createIndex(Indexes.ascending("type"));
    }

    @Override
    public Optional<Person> getPerson(final String personKey) {
        final Bson filter;
        if (ObjectId.isValid(personKey)) {
            filter = eq(ID, new ObjectId(personKey));
        } else {
            filter = or(
                eq(USER_NAME, personKey),
                eq(MESSENGER_ID, personKey),
                eq(PHONE_NUMBER, personKey)
            );
        }

        return Optional.ofNullable(users.find(filter).first());
    }

    @Override
    public List<Person> allPersons() {
        final Iterable<Person> persons = users.find().sort(Sorts.ascending(USER_NAME));
        return ImmutableList.copyOf(persons);
    }

    @Override
    public boolean insertPerson(final Person person) {
        try {
            users.insertOne(person);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting person: " + person, x);
            return false;
        }
    }

    @Override
    public boolean updatePerson(final Person person) {
        final UpdateResult result = users.replaceOne(eq(ID, person.id()), person);
        return result.wasAcknowledged();
    }

    @Override
    public boolean removePerson(final Person person) {
        final DeleteResult result = users.deleteOne(eq(ID, person.id()));
        return result.wasAcknowledged();
    }

    @Override
    public boolean insertBacCalculation(final BacCalculation bacCalculation) {
        try {
            bacCalculations.insertOne(bacCalculation);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting bacCalculation: " + bacCalculation, x);
            return false;
        }
    }

    @Override
    public List<BacCalculation> getBacCalculations(
        final Person person,
        final Optional<ZonedDateTime> fromTime,
        final Optional<ZonedDateTime> toTime
    ) {
        final ImmutableList.Builder<Bson> builder = ImmutableList.builder();
        builder.add(eq(USER_ID, person.id()));
        fromTime.ifPresent(x -> builder.add(gte(TIMESTAMP, x.toInstant().toEpochMilli())));
        toTime.ifPresent(x -> builder.add(lte(TIMESTAMP, x.toInstant().toEpochMilli())));

        final Iterable<BacCalculation> checkins = bacCalculations.find(and(builder.build()))
            .sort(ORDER_BY_TIMESTAMP_ASC);
        return ImmutableList.copyOf(checkins);
    }

    @Override
    public boolean insertCheckin(final Checkin checkin) {
        try {
            checkins.insertOne(checkin);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting checkin: " + checkin, x);
            return false;
        }
    }

    @Override
    public boolean insertCheckins(final List<Checkin> checkins) {
        try {
            // Mongo does not allow empty mass inserts...
            if (!checkins.isEmpty()) {
                this.checkins.insertMany(checkins);
            }
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting checkin: " + checkins, x);
            return false;
        }
    }

    @Override
    public boolean removeCheckin(final Checkin checkin) {
        return checkins.deleteOne(eq(ID, checkin.id())).wasAcknowledged();
    }

    @Override
    public boolean removeCheckins(final Person person) {
        return checkins.deleteMany(eq(USER_ID, person.id())).wasAcknowledged();
    }

    @Override
    public List<Checkin> getCheckins(
        final Person person,
        final Optional<ZonedDateTime> fromTime,
        final Optional<ZonedDateTime> toTime
    ) {
        final ImmutableList.Builder<Bson> builder = ImmutableList.builder();
        builder.add(eq(USER_ID, person.id()));
        fromTime.ifPresent(x -> builder.add(gte(TIMESTAMP, x.toInstant().toEpochMilli())));
        toTime.ifPresent(x -> builder.add(lte(TIMESTAMP, x.toInstant().toEpochMilli())));

        final Iterable<Checkin> checkins = this.checkins.find(and(builder.build()))
            .sort(ORDER_BY_TIMESTAMP_ASC);
        return ImmutableList.copyOf(checkins);
    }

    @Override
    public Optional<Checkin> getCheckin(final String checkinId) {
        final Checkin checkin = checkins.find(eq(ID, new ObjectId(checkinId))).first();
        return Optional.ofNullable(checkin);
    }

    @Override
    public boolean updateCheckin(final Checkin checkin) {
        final UpdateResult result = checkins.replaceOne(eq(ID, checkin.id()), checkin);
        return result.wasAcknowledged();
    }

    @Override
    public boolean insertNotification(final Notification notification) {
        try {
            notifications.insertOne(notification);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting checkin: " + notification, x);
            return false;
        }
    }

    @Override
    public boolean insertNotifications(final List<Notification> notifications) {
        try {
            // Mongo does not allow empty mass inserts...
            if (!notifications.isEmpty()) {
                this.notifications.insertMany(notifications);
            }
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting checkin: " + notifications, x);
            return false;
        }
    }

    @Override
    public List<Notification> unpushedNotifications() {
        final Iterable<Notification> notifications = this.notifications.find(eq("pushed", false))
            .sort(ORDER_BY_TIMESTAMP_ASC);
        return ImmutableList.copyOf(notifications);
    }

    @Override
    public boolean removeNotification(final Notification notification) {
        return notifications.deleteOne(eq(ID, notification.id())).wasAcknowledged();
    }

    @Override
    public List<Notification> getNotifications(
        final Person person,
        final Optional<ZonedDateTime> fromTime,
        final Optional<ZonedDateTime> toTime
    ) {
        final ImmutableList.Builder<Bson> builder = ImmutableList.builder();
        builder.add(eq(USER_ID, person.id()));
        fromTime.ifPresent(x -> builder.add(gte(TIMESTAMP, x.toInstant().toEpochMilli())));
        toTime.ifPresent(x -> builder.add(lte(TIMESTAMP, x.toInstant().toEpochMilli())));

        final Iterable<Notification> notifications = this.notifications.find(and(builder.build()))
            .sort(ORDER_BY_TIMESTAMP_ASC);
        return ImmutableList.copyOf(notifications);
    }

    public Optional<Notification> getNotification(final String notificationId) {
        final Notification notification = notifications.find(eq(ID, new ObjectId(notificationId))).first();
        return Optional.ofNullable(notification);
    }

    @Override
    public boolean updateNotification(final Notification notification) {
        final UpdateResult result = notifications.replaceOne(eq(ID, notification.id()), notification);
        return result.wasAcknowledged();
    }

    @Override
    public boolean markAllReadNotifications(final Person user) {
        final UpdateResult result = notifications.updateMany(
            eq(USER_ID, user.id()),
            set("read", true)
        );
        return result.wasAcknowledged();
    }

    @Override
    public List<Notification> unreadNotifications(final Person user) {
        final Iterable<Notification> notifications = this.notifications.find(
            and(
                eq(USER_ID, user.id()),
                eq("read", false)
            )
        ).sort(ORDER_BY_TIMESTAMP_ASC);
        return ImmutableList.copyOf(notifications);
    }

    @Override
    public boolean insertJob(final JobOptions jobOptions) {
        try {
            jobs.insertOne(jobOptions);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting job: " + jobOptions, x);
            return false;
        }
    }

    @Override
    public boolean removeJob(final JobOptions jobOptions) {
        return jobs.deleteOne(eq(ID, jobOptions.id())).wasAcknowledged();
    }

    @Override
    public List<JobOptions> allRunningJobs(final ZonedDateTime now) {
        final Bson query = or(eq(STOP_TIME, null), gt(STOP_TIME, now.toInstant().toEpochMilli()));
        final Iterable<JobOptions> jobs = this.jobs.find(query).sort(ORDER_BY_START_TIME_ASC);
        return ImmutableList.copyOf(jobs);
    }

    @Override
    public List<JobOptions> getJobs(final Person person) {
        final Iterable<JobOptions> jobs = this.jobs.find(eq(USER_ID, person.id())).sort(ORDER_BY_START_TIME_ASC);
        return ImmutableList.copyOf(jobs);
    }

    @Override
    public Optional<JobOptions> getJob(final String jobId) {
        final JobOptions job = jobs.find(eq(ID, new ObjectId(jobId))).first();
        return Optional.ofNullable(job);
    }

    @Override
    public boolean stopJob(final JobOptions job, final ZonedDateTime stopTime) {
        final UpdateResult result = jobs.updateOne(
            eq(ID, job.id()),
            set(STOP_TIME, stopTime.toInstant().toEpochMilli())
        );
        return result.wasAcknowledged();
    }

    @Override
    public boolean updateJob(final JobOptions job, final ZonedDateTime lastModified) {
        final UpdateResult result = jobs.updateOne(
            eq(ID, job.id()),
            set(LAST_MODIFIED, lastModified.toInstant().toEpochMilli())
        );
        return result.wasAcknowledged();
    }

    @Override
    public Optional<JobOptions> getRunningJob(final Person person, final ZonedDateTime now) {
        final Bson stopTime = or(eq(STOP_TIME, null), gt(STOP_TIME, now.toInstant().toEpochMilli()));
        final Bson query = and(in(USER_ID, person.id()), stopTime);
        final Iterable<JobOptions> jobs = this.jobs.find(query).sort(ORDER_BY_START_TIME_DESC).limit(1);
        return Streams.findLast(Streams.stream(jobs));
    }

    @Override
    public boolean addFollower(final Person person, final Person follower) {
        final Follower follow = new Follower.Builder()
            .userId(person.id())
            .followerId(follower.id())
            .build();

        try {
            followers.insertOne(follow);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting follow: " + follow, x);
            return false;
        }
    }

    @Override
    public boolean removeFollower(final Person person, final Person follower) {
        return followers.deleteOne(
            and(
                eq(FOLLOWING_ID, person.id()),
                eq(FOLLOWER_ID, follower.id())
            )
        ).wasAcknowledged();
    }

    @Override
    public Set<Person> getFollowers(final Person person) {
        final Iterable<Follower> followers = this.followers.find(eq(FOLLOWING_ID, person.id()));
        final Set<ObjectId> ids = Streams.stream(followers)
            .map(Follower::followerId)
            .collect(Collectors.toSet());
        final Iterable<Person> persons = users.find(Filters.in(ID, ids));
        return ImmutableSet.copyOf(persons);
    }

    @Override
    public Set<Person> getFollowing(final Person follower) {
        final Iterable<Follower> following = followers.find(eq(FOLLOWER_ID, follower.id()));
        final Set<ObjectId> ids = Streams.stream(following)
            .map(Follower::userId)
            .collect(Collectors.toSet());
        final Iterable<Person> persons = users.find(Filters.in(ID, ids));
        return ImmutableSet.copyOf(persons);
    }

    @Override
    public Optional<LinkCode> getLinkCode(final Person person, final LinkType type) {
        final Bson query = and(eq(USER_ID, person.id()), eq("type", type.name()));
        final Iterable<LinkCode> jobs = linkCodes.find(query).sort(ORDER_BY_TIMESTAMP_DESC).limit(1);
        return Streams.findLast(Streams.stream(jobs));
    }

    @Override
    public boolean insertLinkCode(final LinkCode linkCode) {
        try {
            linkCodes.insertOne(linkCode);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting link code for user: " + linkCode.userId(), x);
            return false;
        }
    }

    @Override
    public Optional<UntappdLink> getUntappdLink(final Person person) {
        return Optional.ofNullable(untappdLinks.find(eq(USER_ID, person.id())).first());
    }

    @Override
    public Optional<UntappdLink> getUntappdLink(final String untappdUserName) {
        return Optional.ofNullable(untappdLinks.find(eq(UNTAPPD_NAME, untappdUserName)).first());
    }

    @Override
    public boolean insertUntappdLink(final UntappdLink link) {
        try {
            untappdLinks.insertOne(link);
            return true;
        } catch (final Throwable x) {
            LOGGER.info("Error when inserting untappd link: " + link, x);
            return false;
        }
    }

    @Override
    public boolean updateLinkAccessToken(final UntappdLink link, final String accessToken) {
        final String encryptedAccessToken = keyStore.encrypt(accessToken);
        final UpdateResult result = untappdLinks.updateOne(
            eq(ID, link.id()),
            set("accessToken", encryptedAccessToken)
        );
        return result.wasAcknowledged();
    }

    @VisibleForTesting
    void deleteData() {
        users.drop();
        followers.drop();
        checkins.drop();
        bacCalculations.drop();
        untappdLinks.drop();
        jobs.drop();
        notifications.drop();
    }

    /**
     * Used if we have to force rebuild indexes in different environment.
     */
    void dropIndexes() {
        users.dropIndexes();
        followers.dropIndexes();
        checkins.dropIndexes();
        bacCalculations.dropIndexes();
        untappdLinks.dropIndexes();
        jobs.dropIndexes();
        notifications.dropIndexes();
    }
}
