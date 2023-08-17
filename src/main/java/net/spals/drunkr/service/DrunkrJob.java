package net.spals.drunkr.service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.netflix.governator.annotations.Configuration;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.executor.core.ExecutorServiceFactory;
import net.spals.appbuilder.executor.core.ExecutorServiceFactory.Key;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.*;
import net.spals.drunkr.service.untappd.CheckinProvider;

/**
 * Calculates a user's BAC based on a starting time of drinking and their checkins to Untappd.
 * Then reports their BAC via Twilio's SMS or Facebook Messenger back to the user.
 * <p>
 * We have two sources of checkins. The checkins we store in our database and the ones we gather from third parties, untappd.
 * After getting checkins from outside sources we will store them in our database. So we only poll for checkins after {@link #lastModified}.
 *
 * @author spags
 */
public class DrunkrJob implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrunkrJob.class);
    private final BacMessage bacMessage;
    private final Map<String, CheckinProvider> checkinProviders;
    private final DatabaseService dbService;
    private final ExecutorServiceFactory executorServiceFactory;
    private final FollowersNotifier notifier;
    private final I18nSupport i18nSupport;
    private final BacCalculator calculator;
    private final JobOptions options;
    @SuppressWarnings("FieldMayBeFinal")
    @NotNull
    @Configuration("untappd.checkin.provider")
    private String checkinProviderKey = "api";
    @SuppressWarnings("FieldMayBeFinal")
    @Configuration("job.notification")
    private long notification = 30;
    private CheckinProvider checkinProvider;
    /**
     * This is seeded from {@link JobOptions} which defaults to now unless the job is restarted.
     * We update this value here and in the DB in case of restarts, otherwise we would duplicate the work
     * to get data from Untappd.
     */
    private ZonedDateTime lastModified;
    /**
     * For now this value is only maintain within this implementation and not the DB.
     * This means during restarts we could notify twice...
     */
    private ZonedDateTime lastNotified;

    @Inject
    DrunkrJob(
        final BacMessage bacMessage,
        final Map<String, CheckinProvider> checkinProviders,
        final DatabaseService dbService,
        final ExecutorServiceFactory executorServiceFactory,
        final FollowersNotifier notifier,
        final I18nSupport i18nSupport,
        @Assisted final JobOptions options
    ) {
        this.bacMessage = bacMessage;
        this.checkinProviders = checkinProviders;
        this.dbService = dbService;
        this.executorServiceFactory = executorServiceFactory;
        this.notifier = notifier;
        this.i18nSupport = i18nSupport;
        this.options = options;
        calculator = BacCalculator.get();
        lastModified = options.lastModified();
    }

    @VisibleForTesting
    @PostConstruct
    void setCheckinProvider() {
        LOGGER.info("checkin provider=" + checkinProviderKey);
        checkinProvider = checkinProviders.get(checkinProviderKey);
    }

    @Override
    public void run() {
        final ZonedDateTime now = ZonedDateTimes.nowUTC();
        final Optional<Person> optionalPerson = dbService.getPerson(options.userId().toHexString());

        if (!optionalPerson.isPresent()) {
            LOGGER.info("Person no longer exists shutting down job: " + options.userId());
            // Update the stop time as we consider any job with a stop time before now as completed.
            dbService.stopJob(options, now);
            shutdown(options.id());
            return;
        }
        final Person person = optionalPerson.get();
        LOGGER.info("running drunk job for person " + person.userName() + " at " + now + " with last " + lastModified);

        // Drunkr Checkins via our clients.
        final List<Checkin> drunkrCheckins = dbService.getCheckins(
            person,
            Optional.of(options.startTime()),
            Optional.of(now)
        );

        // Untappd Checkins via our untappd services.
        final List<Checkin> untappdCheckins = checkinProvider.get(
            dbService.getUntappdLink(person),
            Optional.of(lastModified)
        );

        // Persist the untappd checkins, but we want to persist after we get the already persisted results.
        final boolean savedCheckins = dbService.insertCheckins(untappdCheckins);
        if (!savedCheckins) {
            // Oops we failed to save checkins, lets log it and try to save next time.
            LOGGER.info("failed to save checkins : " + untappdCheckins);
        } else {
            // Now we've recorded all checkins up to this point so mark lastRun.
            lastModified = now;
            dbService.updateJob(options, lastModified);
        }

        final int totalCheckins = untappdCheckins.size() + drunkrCheckins.size();
        LOGGER.info(
            "number of total checkins for user : "
                + totalCheckins
                + " "
                + person.userName()
        );

        final double gramsOfAlcohol = Stream.concat(untappdCheckins.stream(), drunkrCheckins.stream())
            .map(calculator::drinkToGramsOfAlcohol)
            .reduce(0.0, Double::sum);
        final double bodyWeight = calculator.poundsToGrams(person.weight());
        final double hours = calculator.durationInHours(options.startTime());
        final double bac = calculator.calculate(gramsOfAlcohol, bodyWeight, person.gender(), hours);

        final double roundedBac = Math.round(bac * 1000) / 1000.0;

        saveBac(person, roundedBac, now);

        final String message = i18nSupport.getLabel(
            "job.bac.state",
            person.userName(),
            roundedBac,
            bacMessage.get(bac),
            totalCheckins
        );

        // Only send messages if its outside the timestamp.
        // By default this should be only push a notification to you and your followers every 30 minutes.
        // Otherwise calculations will eventually be viewable through the UI.
        final boolean shouldNotify = null == lastNotified
            || ChronoUnit.MINUTES.between(lastNotified, now) >= notification;
        if (shouldNotify) {
            lastNotified = now;
            notifier.notify(person, message, options.source(), now);
        }

        LOGGER.info("finished running drunk job for person " + person.userName());

        // Add a 10 minute window to check in beer after they started to check in beer.
        // Text based gives a 5 minute before start time window, so the 10 minutes after is really 5 minutes.
        // Instead of (now, now + 10) its (now - 5, now + 5).
        if (roundedBac == 0.0 && now.isAfter(options.startTime().plusMinutes(10))) {
            final String soberMessage = "sober canceling job for person=" + person.userName()
                + " startTime=" + options.startTime();
            LOGGER.info(soberMessage);
            // Update the stop time as we consider any job with a stop time before now as completed.
            dbService.stopJob(options, now);
            final Notification sober = new Notification.Builder()
                .userId(person.id())
                .message(i18nSupport.getLabel("job.stopping.sober", person.userName()))
                .source(options.source())
                .timestamp(now)
                .build();
            dbService.insertNotification(sober);
            shutdown(person.id());
        }
        if (options.stopTime().map(now::isAfter).orElse(false)) {
            final String stopMessage = "stop time reached, stopping job for person=" + person.userName()
                + " stopTime=" + options.stopTime();
            LOGGER.info(stopMessage);
            final Notification stopped = new Notification.Builder()
                .userId(person.id())
                .message(i18nSupport.getLabel("job.stopping.stopped", person.userName()))
                .source(options.source())
                .timestamp(now)
                .build();
            dbService.insertNotification(stopped);
            shutdown(person.id());
        }
    }

    private void shutdown(final ObjectId id) {
        final Key key = new Key.Builder(DrunkrJob.class)
            .addTags(id.toHexString())
            .build();
        executorServiceFactory.stop(key);
    }

    private void saveBac(final Person person, final double bac, final ZonedDateTime now) {
        final BacCalculation bacCalculation = new BacCalculation.Builder()
            .userId(person.id())
            .bac(bac)
            .timestamp(now)
            .build();
        dbService.insertBacCalculation(bacCalculation);
    }

    @VisibleForTesting
    ZonedDateTime getLastNotified() {
        return lastNotified;
    }

    @VisibleForTesting
    void setLastNotified(final ZonedDateTime lastNotified) {
        this.lastNotified = lastNotified;
    }

    @VisibleForTesting
    ZonedDateTime getLastModified() {
        return lastModified;
    }
}
