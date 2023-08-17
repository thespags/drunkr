package net.spals.drunkr.jersey;

import static com.google.common.truth.Truth.assertThat;

import static com.googlecode.catchexception.throwable.CatchThrowable.catchThrowable;
import static com.googlecode.catchexception.throwable.CatchThrowable.caughtThrowable;
import static org.mockito.Mockito.when;

import javax.ws.rs.NotFoundException;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Unit tests for {@link NotificationConverter}.
 *
 * @author spags
 */
public class NotificationConverterTest {

    private static final Person DRINKER = Persons.BROCK;
    private static final Notification NOTIFICATION = new Notification.Builder()
        .userId(DRINKER.id())
        .message("Hello world")
        .timestamp(ZonedDateTimes.nowUTC())
        .build();
    @Mock
    private DatabaseService dbService;
    private I18nSupport i18nSupport;
    private NotificationConverter converter;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        i18nSupport = I18nSupports.getEnglish();
        converter = new NotificationConverter(dbService, i18nSupport);
    }

    @Test
    public void notificationConverted() {
        when(dbService.getNotification(NOTIFICATION.id().toHexString())).thenReturn(Optional.of(NOTIFICATION));

        final Notification notification = converter.fromString(NOTIFICATION.id().toHexString());

        assertThat(notification).isEqualTo(NOTIFICATION);
    }

    @Test
    public void notificationDoesNotExist() {
        when(dbService.getNotification(NOTIFICATION.id().toHexString())).thenReturn(Optional.empty());

        catchThrowable(() -> converter.fromString(NOTIFICATION.id().toHexString()));

        assertThat(caughtThrowable()).hasMessageThat()
            .isEqualTo(i18nSupport.getLabel("invalid.notification", NOTIFICATION.id()));
        assertThat(caughtThrowable()).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void notificationToString() {
        final String id = converter.toString(NOTIFICATION);

        assertThat(id).isEqualTo(NOTIFICATION.id().toHexString());
    }
}