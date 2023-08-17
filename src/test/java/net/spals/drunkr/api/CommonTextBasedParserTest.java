package net.spals.drunkr.api;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import static com.google.common.base.Functions.identity;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import static org.mockito.Mockito.*;

import static net.spals.drunkr.model.Persons.SPAGS;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.*;
import com.google.common.truth.Truth;

import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.spals.drunkr.api.command.ApiCommand;
import net.spals.drunkr.api.command.CommandType;
import net.spals.drunkr.common.ZonedDateTimes;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.i18n.I18nSupports;
import net.spals.drunkr.model.*;

/**
 * Unit tests for {@link CommonTextBasedParser}.
 *
 * @author spags
 */
public class CommonTextBasedParserTest {

    private static final Person DRUNK = Persons.SPAGS;
    private static final String DRUNK_PHONE_NUMBER = Persons.SPAGS_NUMBER;
    private static final Person OTHER_DRUNK = Persons.BROCK;
    private static final JobOptions JOB = new JobOptions.Builder()
        .userId(DRUNK.id())
        .source(Source.SMS)
        .build();
    @Mock
    private DatabaseService dbService;
    @Captor
    private ArgumentCaptor<Map<String, Object>> captor;
    private Map<CommandType, ApiCommand> commands;
    private I18nSupport i18nSupport;
    private CommonTextBasedParser parser;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        commands = Arrays.stream(CommandType.values())
            .collect(Collectors.toMap(identity(), x -> mock(ApiCommand.class)));
        i18nSupport = I18nSupports.getEnglish();
        parser = new CommonTextBasedParser(
            commands,
            dbService,
            i18nSupport
        );
        when(dbService.getPerson(DRUNK_PHONE_NUMBER)).thenReturn(Optional.of(SPAGS));
        when(dbService.getPerson(OTHER_DRUNK.userName())).thenReturn(Optional.of(OTHER_DRUNK));

        final ApiCommand invalidCommand = commands.get(CommandType.INVALID);
        final Response invalidResponse = ApiError.newError(
            INTERNAL_SERVER_ERROR,
            i18nSupport.getLabel("command.invalid")
        ).asResponseBuilder().build();
        when(invalidCommand.run(any())).thenReturn(invalidResponse);
    }

    @Test
    public void doc() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.all"));
    }

    @Test
    public void docForDoc() {

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc doc");

        Truth.assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.doc"));
    }

    @Test
    public void docInvalid() {

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.invalid"));
    }

    @Test
    public void emptyBody() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void invalidFollowerSubCommand() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower add");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void invalidFollowerNoSubCommand() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void docFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follower");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follower"));
    }

    @Test
    public void inviteFollower() {
        final ApiCommand command = commands.get(CommandType.FOLLOWER_INVITE);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower invite someId");

        verify(command).run(ImmutableMap.of("user", DRUNK, "targetUserId", "someId"));
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void docInviteFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follower invite");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follower_invite"));
    }

    @Test
    public void invalidInviteFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower invite someId barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void removeFollower() {
        final ApiCommand command = commands.get(CommandType.FOLLOWER_REMOVE);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(
            Source.SMS,
            DRUNK_PHONE_NUMBER,
            "follower remove " + OTHER_DRUNK.userName()
        );

        verify(command).run(ImmutableMap.of("user", DRUNK, "targetUser", OTHER_DRUNK));
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void removeFollowerDoesNotExist() {
        final String message = parser.parse(
            Source.SMS,
            DRUNK_PHONE_NUMBER,
            "follower remove barf"
        );

        assertThat(message).isEqualTo(i18nSupport.getLabel("invalid.user", "barf"));
    }

    @Test
    public void docRemoveFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follower remove");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follower_remove"));
    }

    @Test
    public void invalidRemoveFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower remove someId barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void listFollower() {
        final ApiCommand command = commands.get(CommandType.FOLLOWER_LIST);
        when(command.run(any())).thenReturn(Response.ok(ImmutableSet.of(OTHER_DRUNK)).build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower list");

        verify(command).run(ImmutableMap.of("user", DRUNK));
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.follower.list", OTHER_DRUNK.userName()));
    }

    @Test
    public void docListFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follower list");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follower_list"));
    }

    @Test
    public void invalidListFollower() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follower list barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void invalidFollowSubCommand() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow add");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void invalidFollowNoSubCommand() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void docFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follow");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follow"));
    }

    @Test
    public void addFollow() {
        final ApiCommand command = commands.get(CommandType.FOLLOWING_ADD);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow add someId");

        verify(command).run(ImmutableMap.of("user", DRUNK, "targetUserId", "someId"));
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void docAddFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follow add");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follow_add"));
    }

    @Test
    public void invalidInviteFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow invite someId barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void removeFollow() {
        final ApiCommand command = commands.get(CommandType.FOLLOWING_REMOVE);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow remove " + OTHER_DRUNK.userName());

        verify(command).run(ImmutableMap.of("user", DRUNK, "targetUser", OTHER_DRUNK));
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void removeFollowDoesNotExist() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow remove barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("invalid.user", "barf"));
    }

    @Test
    public void docRemoveFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follow remove");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follow_remove"));
    }

    @Test
    public void invalidRemoveFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow remove someId barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void listFollow() {
        final ApiCommand command = commands.get(CommandType.FOLLOWING_LIST);
        when(command.run(any())).thenReturn(Response.ok(ImmutableSet.of(OTHER_DRUNK)).build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow list");

        verify(command).run(ImmutableMap.of("user", DRUNK));
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.follow.list", OTHER_DRUNK.userName()));
    }

    @Test
    public void docListFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc follow list");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.follow_list"));
    }

    @Test
    public void invalidListFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "follow list barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void createUserAlreadyExists() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "create spags male 180");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.create.person.existing"));
    }

    @Test
    public void createUserPhoneNumber() {
        when(dbService.getPerson(DRUNK_PHONE_NUMBER)).thenReturn(Optional.empty());
        final ApiCommand command = commands.get(CommandType.USER_ADD);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "create spags male 180");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        final Person person = (Person) result.get("person");
        assertThat(person.phoneNumber()).hasValue(DRUNK_PHONE_NUMBER);
        assertThat(person.userName()).isEqualTo("spags");
        assertThat(person.weight()).isEqualTo(180.0);
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void createUserMessenger() {
        when(dbService.getPerson("messengerId")).thenReturn(Optional.empty());
        final ApiCommand command = commands.get(CommandType.USER_ADD);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, "messengerId", "create spags male 180");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        final Person person = (Person) result.get("person");
        assertThat(person.messengerId()).hasValue("messengerId");
        assertThat(person.userName()).isEqualTo("spags");
        assertThat(person.weight()).isEqualTo(180.0);
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void docCreateFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc create");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.create"));
    }

    @Test
    public void invalidCreateUser() {
        when(dbService.getPerson(DRUNK_PHONE_NUMBER)).thenReturn(Optional.empty());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "create spags male 180 barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void checkinBeer() {
        final ApiCommand command = commands.get(CommandType.CHECKIN_ADD);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "checkin beer");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        final Checkin checkin = (Checkin) result.get("checkin");
        assertThat(checkin.name()).isEqualTo("Beer");
        assertThat(checkin.size()).isEqualTo(12.0);
        assertThat(checkin.abv()).isEqualTo(.05);
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void checkinWine() {
        final ApiCommand command = commands.get(CommandType.CHECKIN_ADD);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "checkin wine");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        final Checkin checkin = (Checkin) result.get("checkin");
        assertThat(checkin.name()).isEqualTo("Wine");
        assertThat(checkin.size()).isEqualTo(5.0);
        assertThat(checkin.abv()).isEqualTo(.12);
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void checkinShot() {
        final ApiCommand command = commands.get(CommandType.CHECKIN_ADD);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "checkin shot");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        final Checkin checkin = (Checkin) result.get("checkin");
        assertThat(checkin.name()).isEqualTo("Shot");
        assertThat(checkin.size()).isEqualTo(1.5);
        assertThat(checkin.abv()).isEqualTo(.40);
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void docCheckinFollow() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc checkin");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.checkin"));
    }

    @Test
    public void invalidCheckinSubCommand() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "checkin");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void invalidCheckinNoSubCommand() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "checkin barf");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.invalid"));
    }

    @Test
    public void checkNoBacCalculations() {
        when(dbService.getBacCalculations(DRUNK, Optional.empty(), Optional.empty())).thenReturn(ImmutableList.of());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "check");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.check.none"));
    }

    @Test
    public void checkWithBacCalculations() {
        final BacCalculation calculation = new BacCalculation.Builder()
            .userId(DRUNK.id())
            .timestamp(ZonedDateTimes.nowUTC())
            .bac(.05)
            .build();
        when(dbService.getBacCalculations(DRUNK, Optional.empty(), Optional.empty())).thenReturn(ImmutableList.of(
            calculation));

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "check");

        assertThat(message).isEqualTo(
            i18nSupport.getLabel("command.check", DRUNK.userName(), calculation.bac(), calculation.timestamp())
        );
    }

    @Test
    public void docCheckin() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc check");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.check"));
    }

    @Test
    public void begin() {
        final ApiCommand command = commands.get(CommandType.JOB_START);
        when(command.run(any())).thenReturn(Response.ok().build());

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "begin");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        assertThat(result).containsKey("jobOptions");
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void docBegin() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc begin");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.begin"));
    }

    @Test
    public void finish() {
        final ApiCommand command = commands.get(CommandType.JOB_STOP);
        when(command.run(any())).thenReturn(Response.ok().build());
        when(dbService.getJobs(DRUNK)).thenReturn(ImmutableList.of(JOB));

        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "finish");

        verify(command).run(captor.capture());
        final Map<String, Object> result = captor.getValue();
        assertThat(result).containsEntry("job", JOB);
        assertThat(message).isEqualTo(i18nSupport.getLabel("command.processed"));
    }

    @Test
    public void docFinish() {
        final String message = parser.parse(Source.SMS, DRUNK_PHONE_NUMBER, "doc finish");

        assertThat(message).isEqualTo(i18nSupport.getLabel("command.doc.finish"));
    }
}