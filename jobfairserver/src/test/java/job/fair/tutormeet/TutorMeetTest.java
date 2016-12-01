package job.fair.tutormeet;

import job.fair.jobfair.testutil.TestUtil;
import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

/**
 * Created by annawang on 2/1/16.
 */
public class TutorMeetTest {

    private static String apiToken;
    private TutorMeet server = new TutorMeet();

    @Test
    public void testLogin() throws Exception {
        JSONObject result = server.login("alex@tutormeet.com", "tutormeet1");
        System.out.println(result.toString());
        assertEquals("Alex Cone", result.getString("name"));
        assertThat(result.getString("token"), not(isEmptyOrNullString()));
        apiToken = result.getString("apiToken");
        assertThat(apiToken, not(isEmptyOrNullString()));
    }

    @Test
    public void testLoginFailed() throws Exception {
        JSONObject result = server.login(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, "bad-password");
        assertEquals(null, result);
    }

    @Test
    public void testSignup() throws Exception {
        JSONObject result = server.signup("unittest@unit.Preload51job.com", TestUtil.TUTORMEET_TEST_NAME_VALUE, TestUtil.TUTORMEET_TEST_PASSWORD, "en");
        assertEquals(0, result.getInt("returnCode"));
    }

    @Test
    public void testSignupFailedWithDuplicateUser() throws Exception {
        JSONObject result = server.signup(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, TestUtil.TUTORMEET_TEST_NAME_VALUE, TestUtil.TUTORMEET_TEST_PASSWORD, "en");
        assertEquals("User has already signup", result.getString("message"));
        assertEquals(-3, result.getInt("returnCode"));
    }

    @Test
    public void testzGetMeetingId() throws Exception {
        JSONObject result = server.getMeetingID("", apiToken);
        System.out.println(result.toString());
    }
}