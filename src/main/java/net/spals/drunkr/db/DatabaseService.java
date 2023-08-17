package net.spals.drunkr.db;

import java.time.ZonedDateTime;
import java.util.*;

import net.spals.drunkr.model.*;

/**
 * Database service interface defining contract for interacting with storage back-end.
 *
 * @author jbrock
 */
public interface DatabaseService {

    /**
     * @param personKey - a user's untappd username, phone number, or messenger id
     * @return {@link Person} if found otherwise empty
     */
    Optional<Person> getPerson(String personKey);

    /**
     * An sorted ordering of all the users in the system by {@link Person#userName()}.
     *
     * @return all users sorted by {@link Person#userName()}
     */
    List<Person> allPersons();

    boolean insertPerson(Person person);

    boolean updatePerson(Person person);

    boolean removePerson(Person person);

    boolean insertBacCalculation(BacCalculation bacCalculation);

    /**
     * A sorted ordering of {@link BacCalculation}s for the given user by {@link BacCalculation#timestamp()}.
     *
     * @param person   the person whose calculations we want
     * @param fromTime the earliest timestamp
     * @param toTime   the latest timestamp
     * @return {@link BacCalculation}s in the provided range sorted by timestamp
     */
    List<BacCalculation> getBacCalculations(
        Person person,
        Optional<ZonedDateTime> fromTime,
        Optional<ZonedDateTime> toTime
    );

    boolean insertCheckin(Checkin checkin);

    boolean insertCheckins(List<Checkin> checkins);

    boolean removeCheckin(Checkin checkin);

    boolean removeCheckins(Person person);

    /**
     * A sorted ordering of {@link Checkin}s for the given user by {@link Checkin#timestamp()}.
     *
     * @param person   the person whose checkins we want
     * @param fromTime the earliest timestamp
     * @param toTime   the latest timestamp
     * @return {@link Checkin}s in the provided range sorted by timestamp
     */
    List<Checkin> getCheckins(Person person, Optional<ZonedDateTime> fromTime, Optional<ZonedDateTime> toTime);

    Optional<Checkin> getCheckin(String checkinId);

    boolean updateCheckin(Checkin checkin);

    boolean insertNotification(Notification notification);

    boolean insertNotifications(List<Notification> notification);

    /**
     * All {@link Notification}s for which {@link Notification#pushed()} evaluates to false, and sorted by {@link Notification#timestamp()}.
     *
     * @return {@link Notification}s that have not been pushed sorted by timestamp
     */
    List<Notification> unpushedNotifications();

    boolean removeNotification(Notification notification);

    /**
     * A sorted ordering of {@link Notification}s for the given user by {@link Notification#timestamp()}.
     *
     * @param person   the person whose notifications we want
     * @param fromTime the earliest timestamp
     * @param toTime   the latest timestamp
     * @return {@link Notification}s in the provided range sorted by timestamp
     */
    List<Notification> getNotifications(
        Person person,
        Optional<ZonedDateTime> fromTime,
        Optional<ZonedDateTime> toTime
    );

    Optional<Notification> getNotification(String notificationId);

    boolean updateNotification(Notification notification);

    boolean markAllReadNotifications(Person user);

    List<Notification> unreadNotifications(Person user);

    boolean insertJob(JobOptions jobOptions);

    boolean removeJob(JobOptions jobOptions);

    /**
     * An ordering of all running jobs in the system by {@link JobOptions#startTime()}.
     * That is job's with {@link JobOptions#stopTime()} that is empty or after now.
     *
     * @return all running jobs sorted by {@link JobOptions#startTime()}
     */
    List<JobOptions> allRunningJobs(ZonedDateTime now);

    /**
     * An ordering of all jobs for the given user in the system by {@link JobOptions#startTime()}.
     * That is job's with {@link JobOptions#stopTime()} that is empty or after now.
     *
     * @return all jobs for the given user, active or not, sorted by {@link JobOptions#startTime()}
     */
    List<JobOptions> getJobs(Person person);

    Optional<JobOptions> getJob(String jobId);

    boolean stopJob(JobOptions job, ZonedDateTime stopTime);

    boolean updateJob(JobOptions job, ZonedDateTime lastModified);

    /**
     * Gets the running {@link JobOptions} for a user if one exists. This assumes we have at most one job running.
     * If we have more than one, which would be a bug some where else, then this returns the one with the latest start time.
     * Given the current time as now, this consider a job running if the stop time of a job is after now.
     *
     * @param person the user to look for a running job
     * @param now    the current time in GMT
     * @return the running job if the person has a job with a stop time after now, otherwise empty
     */
    Optional<JobOptions> getRunningJob(Person person, ZonedDateTime now);

    boolean addFollower(Person following, Person follower);

    boolean removeFollower(Person following, Person follower);

    /**
     * An unordered collection of users following the given user.
     *
     * @param following the user to look fo followers
     * @return a set of users following the given user
     */
    Set<Person> getFollowers(Person following);

    /**
     * An unordered collection of users who the given user follows.
     *
     * @param follower the user to look fo followings
     * @return a set of users the given user follows
     */
    Set<Person> getFollowing(Person follower);

    /**
     * Gets the latest {@link LinkCode} for the given {@link LinkType}
     *
     * @param person the user to look for the link codes
     * @param type   the type of link code to get
     * @return the latest link code of the given link type
     */
    Optional<LinkCode> getLinkCode(Person person, LinkType type);

    boolean insertLinkCode(LinkCode linkCode);

    Optional<UntappdLink> getUntappdLink(Person person);

    Optional<UntappdLink> getUntappdLink(String untappdUserName);

    boolean insertUntappdLink(UntappdLink link);

    boolean updateLinkAccessToken(UntappdLink link, String accessToken);
}
