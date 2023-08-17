package net.spals.drunkr.db;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static net.spals.drunkr.model.LinkType.AUTH_USER;
import static net.spals.drunkr.model.LinkType.LINK_PHONE;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import org.bson.types.ObjectId;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;

import net.spals.appbuilder.keystore.core.KeyStore;
import net.spals.drunkr.common.HasTimestampCorrespondence;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.model.*;
import net.spals.drunkr.serialization.ObjectMappers;

/**
 * Tests for Mongo DB implementation. Requires MongoDB to be running for tests to execute.
 *
 * @author jbrock
 */
public class MongoDbITest {

    /**
     * mongodb://[dbuser:dbpassword@]host:port/dbname
     */
    private static final String MONGO_URI = Optional.ofNullable(Strings.emptyToNull(System.getenv("MONGODB_URI")))
        .orElse("mongodb://localhost:27017/drunkr_test");
    private static final String PERSON_PHONE = "+1555555555";
    private static final double PERSON_WEIGHT = 150.0;
    private static final Gender PERSON_GENDER = Gender.FEMALE;
    private static final String PERSON_NAME = "Test";
    private static final Random RANDOM = new Random();
    private final Person person, otherPerson;
    private final ZonedDateTime past, present, future;
    private final Checkin checkin;
    private final List<Checkin> checkins;
    private final Notification notification;
    private final List<Notification> notifications;
    private final JobOptions job;
    private final LinkCode linkCode, oldLinkCode;
    @Mock
    private KeyStore keystore;
    private MongoDb dbService;

    public MongoDbITest() {
        person = buildTestPerson();
        otherPerson = buildTestPerson();
        present = ZonedDateTimes.nowUTC();
        past = present.minusMinutes(10);
        future = present.plusMinutes(10);
        checkin = createTimestampedCheckin(present);
        checkins = ImmutableList.of(
            createTimestampedCheckin(past),
            checkin,
            createTimestampedCheckin(future)
        );
        notification = createTimestampedNotification(present);
        notifications = ImmutableList.of(
            createTimestampedNotification(past),
            notification,
            createTimestampedNotification(future)
        );
        job = new JobOptions.Builder()
            .userId(person.id())
            .startTime(present)
            .source(Source.SMS)
            .build();
        linkCode = createTimestampedLinkCode(present);
        oldLinkCode = createTimestampedLinkCode(past);
    }

    @BeforeClass
    public void classSetUp() {
        MockitoAnnotations.initMocks(this);

        final MongoDbProvider provider = new MongoDbProvider(ObjectMappers.mongoMapper());
        provider.createDb(MONGO_URI);

        // Don't encrypt any of our information for db testing purposes.
        when(keystore.encrypt(anyString())).thenAnswer(i -> i.getArguments()[0]);
        dbService = new MongoDb(provider.get(), keystore);
        // App integration tests may add data so delete now.
        dbService.deleteData();
        dbService.buildIndexes();
    }

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterClass
    public void classTearDown() {
        dbService.deleteData();
    }

    private LinkCode createTimestampedLinkCode(final ZonedDateTime timestamp) {
        return new LinkCode.Builder()
            .userId(person.id())
            .link("link")
            .code("code")
            .type(LINK_PHONE)
            .timestamp(timestamp)
            .build();
    }

    private Checkin createTimestampedCheckin(final ZonedDateTime timestamp) {
        return new Checkin.Builder()
            .userId(person.id())
            .name("Duff")
            .style(Style.BOTTLE)
            .size(Style.BOTTLE.getServingSize())
            .timestamp(timestamp)
            .abv(.05)
            .build();
    }

    private Notification createTimestampedNotification(final ZonedDateTime timestamp) {
        return new Notification.Builder()
            .userId(person.id())
            .message("Hello World")
            .timestamp(timestamp)
            .build();
    }

    private BacCalculation createTimestampedBacCalculation(final ZonedDateTime timestamp) {
        return new BacCalculation.Builder()
            .userId(person.id())
            .bac(.05)
            .timestamp(timestamp)
            .build();
    }

    private Person buildTestPerson() {
        return new Person.Builder()
            .userName(PERSON_NAME + RANDOM.nextInt())
            .gender(PERSON_GENDER)
            .weight(PERSON_WEIGHT)
            .phoneNumber(PERSON_PHONE + RANDOM.nextInt())
            .build();
    }

    private UntappdLink buildTestUntappdLink(final ObjectId userId) {
        return new UntappdLink.Builder()
            .untappdName("untappd" + RANDOM.nextInt())
            .userId(userId)
            .build();
    }

    @Test
    public void insertPerson() {
        final boolean inserted = dbService.insertPerson(person);

        assertThat(inserted).isEqualTo(true);
    }

    @Test
    public void insertOtherPerson() {
        final boolean inserted = dbService.insertPerson(otherPerson);

        assertThat(inserted).isEqualTo(true);
    }

    // After all other removals have completed, we can safely modify the user then remove the user.
    @Test(dependsOnMethods = { "removeFollower", "removeAllCheckins" })
    public void updatePerson() {
        final String messengerId = "messengerFoo" + RANDOM.nextInt();
        final Person patch = new Person.Builder()
            .mergeFrom(person)
            .messengerId(messengerId)
            .build();
        final boolean updated = dbService.updatePerson(patch);

        assertThat(updated).isEqualTo(true);
        final Optional<Person> updatedUser = dbService.getPerson(person.userName());
        assertThat(updatedUser).isPresent();
        updatedUser.ifPresent(person -> assertThat(person.messengerId()).hasValue(messengerId));
    }

    @Test(dependsOnMethods = "updatePerson")
    public void removePerson() {
        final boolean deleted = dbService.removePerson(person);

        assertThat(deleted).isEqualTo(true);
    }

    @Test(dependsOnMethods = { "removePerson" })
    public void removeOtherPerson() {
        final boolean deleted = dbService.removePerson(otherPerson);

        assertThat(deleted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertPerson")
    public void getPersonByUserName() {
        final Optional<Person> foundPerson = dbService.getPerson(person.userName());

        assertThat(foundPerson).hasValue(person);
    }

    @Test(dependsOnMethods = "insertPerson")
    public void insertPersonByUserNameInsensitive() {
        final Person duplicateName = new Person.Builder()
            .weight(125)
            .gender(Gender.MALE)
            .userName(person.userName().toUpperCase())
            .build();

        final boolean added = dbService.insertPerson(duplicateName);

        assertThat(added).isFalse();
    }

    @Test(dependsOnMethods = "insertPerson")
    public void getPersonById() {
        final Optional<Person> foundPerson = dbService.getPerson(person.id().toHexString());

        assertThat(foundPerson).hasValue(person);
    }

    @Test(dependsOnMethods = "insertPerson")
    public void getPersonNoResults() {
        final Optional<Person> foundPerson = dbService.getPerson(PERSON_NAME + RANDOM.nextInt());

        assertThat(foundPerson).isEmpty();
    }

    @Test
    public void insertCheckin() {
        final boolean inserted = dbService.insertCheckin(checkin);

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertCheckin")
    public void getCheckin() {
        final Optional<Checkin> queriedCheckin = dbService.getCheckin(checkin.id().toHexString());

        assertThat(queriedCheckin).hasValue(checkin);
    }

    @Test(dependsOnMethods = "getCheckin")
    public void updateCheckin() {
        final Checkin updatedCheckin = new Checkin.Builder()
            .mergeFrom(checkin)
            .abv(.08)
            .build();

        final boolean result = dbService.updateCheckin(updatedCheckin);
        final Optional<Checkin> queriedCheckin = dbService.getCheckin(updatedCheckin.id().toHexString());

        assertThat(result).isTrue();
        assertThat(queriedCheckin).hasValue(updatedCheckin);
    }

    @Test(dependsOnMethods = "updateCheckin")
    public void removeCheckin() {
        final boolean removed = dbService.removeCheckin(checkin);

        assertThat(removed).isEqualTo(true);
    }

    @Test(dependsOnMethods = "removeCheckin")
    public void insertCheckins() {
        final boolean inserted = dbService.insertCheckins(checkins);

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "removeCheckin")
    public void insertEmptyCheckins() {
        final boolean inserted = dbService.insertCheckins(ImmutableList.of());

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertCheckins")
    public void checkinsSorted() {
        // Verify checkins are sorted when directly queried
        final List<Checkin> checkins = dbService.getCheckins(
            person,
            Optional.of(present.minusMinutes(20)),
            Optional.of(future)
        );

        assertThat(checkins).comparingElementsUsing(HasTimestampCorrespondence.get())
            .containsExactly(past, present, future);
    }

    @Test(dependsOnMethods = "checkinsSorted")
    public void checkinsFilteredByTime() {
        final List<Checkin> checkins = dbService.getCheckins(
            person,
            Optional.of(present.minusMinutes(5)),
            Optional.of(present.plusMinutes(5))
        );

        assertThat(checkins).containsExactly(checkin);
    }

    @Test(dependsOnMethods = "checkinsFilteredByTime")
    public void removeAllCheckins() {
        final boolean deleted = dbService.removeCheckins(person);

        assertThat(deleted).isEqualTo(true);
    }

    @Test
    public void insertNotification() {
        final boolean inserted = dbService.insertNotification(notification);

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertNotification")
    public void getNotification() {
        final Optional<Notification> queriedNotification = dbService.getNotification(notification.id().toHexString());

        assertThat(queriedNotification).hasValue(notification);
    }

    @Test(dependsOnMethods = "insertNotification")
    public void unpushedNotificationsNonEmpty() {
        final List<Notification> notifications = dbService.unpushedNotifications();

        assertThat(notifications).containsExactly(notification);
    }

    @Test(dependsOnMethods = "getNotification")
    public void updateNotification() {
        final Notification updatedNotification = new Notification.Builder()
            .mergeFrom(notification)
            .read(true)
            .pushed(true)
            .build();

        final boolean result = dbService.updateNotification(updatedNotification);
        final Optional<Notification> queriedNotification = dbService.getNotification(updatedNotification.id()
            .toHexString());

        assertThat(result).isTrue();
        assertThat(queriedNotification).hasValue(updatedNotification);
    }

    @Test(dependsOnMethods = "updateNotification")
    public void unpushedNotificationsEmpty() {
        final List<Notification> notifications = dbService.unpushedNotifications();

        assertThat(notifications).isEmpty();
    }

    @Test(dependsOnMethods = "updateNotification")
    public void removeNotification() {
        final boolean removed = dbService.removeNotification(notification);

        assertThat(removed).isEqualTo(true);
    }

    @Test(dependsOnMethods = "removeNotification")
    public void insertNotifications() {
        final boolean inserted = dbService.insertNotifications(notifications);

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "removeNotification")
    public void insertEmptyNotifications() {
        final boolean inserted = dbService.insertNotifications(ImmutableList.of());

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertNotifications")
    public void notificationsSorted() {
        // Verify notifications are sorted when directly queried
        final List<Notification> notifications = dbService.getNotifications(
            person,
            Optional.of(present.minusMinutes(20)),
            Optional.of(future)
        );

        assertThat(notifications).comparingElementsUsing(HasTimestampCorrespondence.get())
            .containsExactly(past, present, future);
    }

    @Test(dependsOnMethods = "insertNotifications")
    public void notificationsFilteredByTime() {
        final List<Notification> notifications = dbService.getNotifications(
            person,
            Optional.of(present.minusMinutes(5)),
            Optional.of(present.plusMinutes(5))
        );

        assertThat(notifications).containsExactly(notification);
    }

    @Test(dependsOnMethods = { "notificationsFilteredByTime", "insertNotifications" })
    public void nonEmptyUnreadNotifications() {
        final List<Notification> updated = dbService.unreadNotifications(person);

        assertThat(updated).containsExactlyElementsIn(notifications);
    }

    @Test(dependsOnMethods = { "nonEmptyUnreadNotifications" })
    public void markAllReadNotifications() {
        final boolean updated = dbService.markAllReadNotifications(person);

        assertThat(updated).isTrue();
    }

    @Test(dependsOnMethods = { "markAllReadNotifications" })
    public void emptyUnreadNotifications() {
        final List<Notification> updated = dbService.unreadNotifications(person);

        assertThat(updated).isEmpty();
    }

    @Test(dependsOnMethods = "insertPerson")
    public void insertBacCalculation() {
        boolean inserted;
        inserted = dbService.insertBacCalculation(createTimestampedBacCalculation(present));
        inserted &= dbService.insertBacCalculation(createTimestampedBacCalculation(future));
        inserted &= dbService.insertBacCalculation(createTimestampedBacCalculation(past));

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertBacCalculation")
    public void bacCalculationsSorted() {
        final List<BacCalculation> calculations = dbService.getBacCalculations(
            person,
            Optional.empty(),
            Optional.empty()
        );

        assertThat(calculations).comparingElementsUsing(HasTimestampCorrespondence.get())
            .containsExactly(past, present, future);
    }

    @Test(dependsOnMethods = "insertBacCalculation")
    public void bacCalculationsFilteredByTime() {
        final List<BacCalculation> calculations = dbService.getBacCalculations(
            person,
            Optional.of(present.minusMinutes(5)),
            Optional.of(present.plusMinutes(5))
        );

        assertThat(calculations).comparingElementsUsing(HasTimestampCorrespondence.get())
            .containsExactly(present);
    }

    @Test(dependsOnMethods = { "insertPerson", "insertOtherPerson" })
    public void addFollower() {
        final boolean inserted = dbService.addFollower(person, otherPerson);

        assertThat(inserted).isEqualTo(true);
    }

    @Test(dependsOnMethods = "addFollower")
    public void addDuplicateFollower() {
        final boolean duplicate = dbService.addFollower(person, otherPerson);

        assertThat(duplicate).isEqualTo(false);
    }

    @Test(dependsOnMethods = "addFollower")
    public void getFollowers() {
        final Set<Person> followers = dbService.getFollowers(person);

        assertThat(followers).containsExactly(otherPerson);
    }

    @Test(dependsOnMethods = "addFollower")
    public void getFollowings() {
        final Set<Person> following = dbService.getFollowing(otherPerson);

        assertThat(following).containsExactly(person);
    }

    @Test(dependsOnMethods = { "addDuplicateFollower", "getFollowers", "getFollowings" })
    public void removeFollower() {
        final boolean removed = dbService.removeFollower(person, otherPerson);

        assertThat(removed).isEqualTo(true);
    }

    @Test
    public void insertJob() {
        final boolean added = dbService.insertJob(job);

        assertThat(added).isEqualTo(true);
    }

    @Test(dependsOnMethods = "insertJob")
    public void hasRunningJobNoStopTime() {
        final Optional<JobOptions> runningJob = dbService.getRunningJob(person, present);

        assertThat(runningJob).hasValue(job);
    }

    @Test(dependsOnMethods = "insertJob")
    public void allRunningJobsNoStopTime() {
        final List<JobOptions> foundJob = dbService.allRunningJobs(present);

        assertThat(foundJob).containsExactly(job);
    }

    @Test(dependsOnMethods = "insertJob")
    public void getJob() {
        final Optional<JobOptions> foundJob = dbService.getJob(job.id().toHexString());

        assertThat(foundJob).hasValue(job);
    }

    @Test(dependsOnMethods = "insertJob")
    public void getJobs() {
        final List<JobOptions> foundJob = dbService.getJobs(person);

        assertThat(foundJob).containsExactly(job);
    }

    @Test(dependsOnMethods = { "getJob", "getJobs", "allRunningJobsNoStopTime", "hasRunningJobNoStopTime" })
    public void stopJob() {
        final boolean updatedJob = dbService.stopJob(job, future);
        final Optional<JobOptions> stoppedJob = dbService.getJob(job.id().toHexString());

        assertThat(stoppedJob).isPresent();
        stoppedJob.ifPresent(x -> assertThat(x.stopTime()).hasValue(future));
        assertThat(updatedJob).isEqualTo(true);
    }

    @Test(dependsOnMethods = "stopJob")
    public void allRunningJobsBeforeStopTime() {
        final JobOptions stoppedJob = new JobOptions.Builder()
            .mergeFrom(job)
            .stopTime(future)
            .build();

        final List<JobOptions> foundJob = dbService.allRunningJobs(past);

        assertThat(foundJob).containsExactly(stoppedJob);
    }

    @Test(dependsOnMethods = "stopJob")
    public void allRunningJobsAfterStopTime() {
        final List<JobOptions> foundJob = dbService.allRunningJobs(future);

        assertThat(foundJob).isEmpty();
    }

    @Test(dependsOnMethods = "stopJob")
    public void hasRunningJobBeforeStopTime() {
        final JobOptions stoppedJob = new JobOptions.Builder()
            .mergeFrom(job)
            .stopTime(future)
            .build();

        final Optional<JobOptions> runningJob = dbService.getRunningJob(person, past);

        assertThat(runningJob).hasValue(stoppedJob);
    }

    @Test(dependsOnMethods = "stopJob")
    public void hasRunningJobAfterStopTime() {
        final Optional<JobOptions> runningJob = dbService.getRunningJob(person, future);

        assertThat(runningJob).isEmpty();
    }

    @Test(dependsOnMethods = { "hasRunningJobBeforeStopTime", "hasRunningJobAfterStopTime" })
    public void updateJob() {
        final boolean updatedJob = dbService.updateJob(job, future);
        final Optional<JobOptions> stoppedJob = dbService.getJob(job.id().toHexString());

        assertThat(stoppedJob).isPresent();
        stoppedJob.ifPresent(x -> assertThat(x.lastModified()).isEqualTo(future));
        assertThat(updatedJob).isEqualTo(true);
    }

    @Test(dependsOnMethods = { "updateJob" })
    public void removeJob() {
        final boolean removed = dbService.removeJob(job);

        assertThat(removed).isEqualTo(true);
    }

    @Test
    public void findUntappdLink() {
        final UntappdLink link = buildTestUntappdLink(person.id());
        dbService.insertUntappdLink(link);

        final Optional<UntappdLink> found = dbService.getUntappdLink(person);

        assertThat(found).hasValue(link);
    }

    @Test
    public void findUntappdLinkByUntappd() {
        final ObjectId userId = new ObjectId();
        final UntappdLink link = buildTestUntappdLink(userId);
        dbService.insertUntappdLink(link);

        final Optional<UntappdLink> found = dbService.getUntappdLink(link.untappdName());

        assertThat(found).hasValue(link);
    }

    @Test
    public void addUntappdLink() {
        final ObjectId userId = new ObjectId();
        final UntappdLink link = buildTestUntappdLink(userId);

        final boolean inserted = dbService.insertUntappdLink(link);
        final Optional<UntappdLink> found = dbService.getUntappdLink(
            Person.Builder
                .from(person)
                .id(userId)
                .build()
        );

        assertThat(inserted).isEqualTo(true);
        assertThat(found).hasValue(link);
    }

    @Test
    public void updateAccessToken() {
        final ObjectId userId = new ObjectId();
        final UntappdLink link = buildTestUntappdLink(userId);
        dbService.insertUntappdLink(link);

        final String accessToken = "foo access";
        dbService.updateLinkAccessToken(link, accessToken);

        final Optional<UntappdLink> found = dbService.getUntappdLink(
            Person.Builder
                .from(person)
                .id(userId)
                .build()
        );

        final UntappdLink expectedLink = new UntappdLink.Builder()
            .mergeFrom(link)
            .accessToken(accessToken)
            .build();
        assertThat(found).hasValue(expectedLink);
    }

    @Test
    public void insertLinkCode() {
        final boolean inserted = dbService.insertLinkCode(linkCode);

        assertThat(inserted).isTrue();
    }

    @Test
    public void insertOldLinkCode() {
        final boolean inserted = dbService.insertLinkCode(oldLinkCode);

        assertThat(inserted).isTrue();
    }

    @Test(dependsOnMethods = { "insertLinkCode", "insertOldLinkCode" })
    public void getLinkCode() {
        // Only get the latest link code.
        final Optional<LinkCode> inserted = dbService.getLinkCode(person, LINK_PHONE);

        assertThat(inserted).hasValue(linkCode);
    }

    @Test(dependsOnMethods = { "insertLinkCode", "insertOldLinkCode" })
    public void getLinkCodeWrongType() {
        // Only get the latest link code.
        final Optional<LinkCode> inserted = dbService.getLinkCode(person, AUTH_USER);

        assertThat(inserted).isEmpty();
    }
}
