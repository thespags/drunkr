package net.spals.drunkr.app;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

import static com.google.common.truth.Truth.assertThat;


import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.util.*;

import com.google.common.collect.ImmutableMap;

import io.dropwizard.Configuration;
import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.testng.annotations.*;

/**
 * Integration tests for the Drunkr Application.
 *
 * @author tkral
 */
public class DrunkrAppITest {

    private static final GenericType<Map<String, Object>> MAP_STRING_TO_OBJECT =
        new GenericType<Map<String, Object>>() {
        };
    private static final GenericType<List<Map<String, Object>>> LIST_MAP_TO_OBJECT =
        new GenericType<List<Map<String, Object>>>() {
        };
    private static final Map<String, Object> TEST_USER = ImmutableMap.<String, Object>builder()
        .put("userName", "testPerson" + UUID.randomUUID())
        .put("gender", "MALE")
        .put("weight", 185.0)
        .build();
    private static final Map<String, Object> OTHER_USER = ImmutableMap.<String, Object>builder()
        .put("userName", "OtherPerson" + UUID.randomUUID())
        .put("gender", "FEMALE")
        .put("weight", 165.0)
        .build();
    private final DropwizardTestSupport<Configuration> drunkrApp;
    private final Client drunkrClient;
    private String testUserId;
    private String otherUserId;
    private Map<String, Object> testUser;
    private Map<String, Object> otherUser;

    public DrunkrAppITest() {
        drunkrApp = new DropwizardTestSupport<>(DrunkrApp.class, new Configuration());
        drunkrClient = ClientBuilder.newBuilder()
            // Add support for non-standard HTTP verbs (i.e. PATCH)
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .build();
    }

    private Map<String, Object> addId(final Map<String, Object> user, final String id) {
        final Map<String, Object> userWithId = new HashMap<>(user);
        userWithId.put("_id", id);
        userWithId.put("phoneNumber", null);
        userWithId.put("messengerId", null);
        return userWithId;
    }

    @BeforeClass
    void classSetUp() {
        drunkrApp.before();
    }

    @AfterClass
    void classTearDown() {
        drunkrApp.after();
    }

    @Test
    public void createUser() {
        final Invocation.Builder userTarget = constructTarget("users");
        final Response response = userTarget.post(Entity.json(TEST_USER));

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);

        assertThat(json).containsKey("_id");
        testUserId = json.get("_id").toString();
        testUser = addId(TEST_USER, testUserId);
        assertThat(json).isEqualTo(testUser);
    }

    @Test(dependsOnMethods = { "createUser" })
    public void createOtherUser() {
        final Invocation.Builder userTarget = constructTarget("users");
        final Response response = userTarget.post(Entity.json(OTHER_USER));

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);

        assertThat(json).containsKey("_id");
        otherUserId = json.get("_id").toString();
        otherUser = addId(OTHER_USER, otherUserId);
        assertThat(json).isEqualTo(otherUser);
    }

    @Test(dependsOnMethods = { "createUser" })
    public void getUser() {
        final Invocation.Builder userTarget = constructTarget("users/" + testUserId);
        final Response response = userTarget.get();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);
        assertThat(json).isEqualTo(testUser);
    }

    @Test(dependsOnMethods = { "createOtherUser" })
    public void follow() {
        final Invocation.Builder userTarget = constructTarget("users/" + testUserId + "/following");

        final Map<String, Object> payload = ImmutableMap.of("targetUserId", otherUserId);
        final Response response = userTarget.post(Entity.json(payload));

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);
        assertThat(json).containsExactly(
            "followee", otherUser,
            "follower", testUser
        );
    }

    @Test(dependsOnMethods = { "createOtherUser" })
    public void followOther() {
        final Invocation.Builder userTarget = constructTarget("users/" + otherUserId + "/following");

        final Map<String, Object> payload = ImmutableMap.of("targetUserId", testUserId);
        final Response response = userTarget.post(Entity.json(payload));

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);
        assertThat(json).containsExactly(
            "followee", testUser,
            "follower", otherUser
        );
    }

    @Test(dependsOnMethods = { "follow" })
    public void allFollowing() {
        final Invocation.Builder userTarget = constructTarget("users/" + testUserId + "/following");

        final Response response = userTarget.get();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        final List<Map<String, Object>> json = response.readEntity(LIST_MAP_TO_OBJECT);

        assertThat(json).containsExactly(otherUser);
    }

    @Test(dependsOnMethods = { "follow" })
    public void allFollowers() {
        final Invocation.Builder userTarget = constructTarget("users/" + otherUserId + "/followers");

        final Response response = userTarget.get();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        final List<Map<String, Object>> json = response.readEntity(LIST_MAP_TO_OBJECT);

        assertThat(json).containsExactly(addId(TEST_USER, testUserId));
    }

    @Test(dependsOnMethods = { "allFollowing", "allFollowers" })
    public void removeFollowing() {
        final Invocation.Builder userTarget = constructTarget(
            "users/" + testUserId + "/following/" + otherUserId
        );

        final Response response = userTarget.delete();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);

        assertThat(json).containsEntry("_id", otherUserId);
    }

    @Test(dependsOnMethods = { "removeFollowing" })
    public void inviteToFollow() {
        final Invocation.Builder userTarget = constructTarget("users/" + otherUserId + "/followers");

        final Map<String, Object> payload = ImmutableMap.of("targetUserId", testUserId);
        final Response response = userTarget.post(Entity.json(payload));

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);

        assertThat(json).containsEntry("_id", testUserId);
    }

    @Test(dependsOnMethods = { "followOther" })
    public void removeFollower() {
        final Invocation.Builder userTarget = constructTarget(
            "users/" + testUserId + "/followers/" + otherUserId
        );

        final Response response = userTarget.delete();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        final Map<String, Object> json = response.readEntity(MAP_STRING_TO_OBJECT);

        assertThat(json).containsEntry("_id", otherUserId);
    }

    private Invocation.Builder constructTarget(final String path) {
        final String address = "http://localhost:" + drunkrApp.getLocalPort();
        return drunkrClient.target(address)
            .path(path)
            .request(MediaType.APPLICATION_JSON_TYPE);
    }
}
