package job.fair.tutormeet;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by annawang on 1/27/16.
 */
public class TutorMeet {
    public static final String JSON_MEETING_SUBJECT = "subject";
    public static final String JSON_MEETING_ID = "meetingId";
    public static final String JSON_MEETING_LOCATION = "location";
    public static final String RETURN_CODE = "returnCode";
    public static final String POST_HEADER_API_TOKEN = "apiToken";
    private static final String TUTORMEET_CORPLOGIN_DO = "/tutormeetweb/corplogin.do";
    private static final String TUTORMEET_MESSAGE_DO = "/tutormeetweb/message.do";
    private static final String TUTORMEET_MEETING_DO = "/tutormeetweb/corpmeeting.do";
    private static final String TUTORMEET_PROPERTIES = "tutormeet.properties";
    private static final String POST_USER_AGENT = "User-Agent";
    public static final String POST_HEADER_USER_AGENT1 = POST_USER_AGENT;
    private static final String POST_USER_AGENT_VALUE = "Mozilla/5.0";
    private static final String POST_ACTION = "action";
    private static final String POST_JSON = "json";
    private static final Logger logger = Logger.getLogger(TutorMeet.class);
    private static final String ACTION_AUTH_USER = "authUser";
    private static final String ACTION_SIGNUP = "userSignup";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_PASSWORD = "password";
    private static final String JSON_NAME = "name";
    private static final String JSON_LOCALE = "locale";
    public static String TUTORMEET_AVATAR_PREFIX;
    private static String PROTOCOL;
    private static String HOSTNAME;
    public String MEETING_URL_PREFIX;

    public TutorMeet() {
        loadProperties();
    }

    public static String getAvatarPrefix(int userSN) {
        return TutorMeet.TUTORMEET_AVATAR_PREFIX + userSN + "_";
    }

    /**
     * Call TutorMeet corplogin.do API with authUser action.
     *
     * @param email    email for login
     * @param password password for login
     * @return the JSONObject with login info. If the JSONObject is empty then <code>null</code> is returned.
     * @throws IOException URL exception
     */
    public JSONObject login(String email, String password) throws IOException {
        JSONObject json = new JSONObject();
        json.put(JSON_EMAIL, email);
        json.put(JSON_PASSWORD, password);
        return sendPOST(TUTORMEET_CORPLOGIN_DO, ACTION_AUTH_USER, json.toString(), null);
    }


    /**
     * Call TutorMeet meesage.do API with userSignup action.
     *
     * @param email    email for signup
     * @param name     name for signup
     * @param password password for signup
     * @param locale   locale for signup
     * @return the JSONObject with returnCode: 0 if signup successful.
     * If signup failed, returnCode won't be 0 with error message in JSONObject.
     * @throws IOException URL exception
     */
    public JSONObject signup(String email, String name, String password, String locale) throws IOException {
        JSONObject json = new JSONObject();
        json.put(POST_ACTION, ACTION_SIGNUP);
        json.put(JSON_EMAIL, email);
        json.put(JSON_PASSWORD, password);
        json.put(JSON_NAME, name);
        json.put(JSON_LOCALE, locale);

        return sendPOST(TUTORMEET_MESSAGE_DO, null, json.toString(), null);
    }

    /**
     * Get Meeting ID
     *
     * @param apiToken apiToken for login to Tutormeet
     * @return json object which contains meetingid
     * @throws IOException URL exception
     */
    public JSONObject getMeetingID(String meetingID, String apiToken) throws IOException {
        logger.info("Try to get meeting id from TutorMeet with apiToken " + apiToken);
        JSONObject json = new JSONObject();
        json.put(JSON_MEETING_ID, meetingID);
        json.put(JSON_MEETING_SUBJECT, "testInterview");
        json.put("material", "E:\\materials-png\\100062\\01.png");
        json.put(JSON_MEETING_LOCATION, "TC");
        json.put(POST_ACTION, "launchInterview");
        JSONObject result = sendPOST(TUTORMEET_MEETING_DO, null, json.toString(), apiToken);
        if (result.getInt(RETURN_CODE) != 0) {
            throw new InternalError("Encounter problem when calling tutormeet");
        }
        return result;
    }

    private JSONObject sendPOST(String url, String action, String json, String apiToken) throws IOException {
        System.setProperty("jsse.enableSNIExtension", "false");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(new URL(PROTOCOL, HOSTNAME, url).toString());
        httpPost.addHeader(POST_HEADER_USER_AGENT1, POST_USER_AGENT_VALUE);
        httpPost.addHeader(POST_HEADER_API_TOKEN, apiToken);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        if (action != null) {
            urlParameters.add(new BasicNameValuePair(POST_ACTION, action));
        }
        urlParameters.add(new BasicNameValuePair(POST_JSON, json));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
        httpPost.setEntity(postParams);

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        if (response.toString().isEmpty()) return null;

        JSONObject result = new JSONObject(response.toString());
        httpClient.close();

        return result;
    }

    private void loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = getClass().getResourceAsStream(TUTORMEET_PROPERTIES);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            PROTOCOL = prop.getProperty("protocol");
            HOSTNAME = prop.getProperty("hostname");
            MEETING_URL_PREFIX = new URL(PROTOCOL, HOSTNAME, "/tutormeet/tutormeet.html?data=").toString();
            TUTORMEET_AVATAR_PREFIX = new URL(PROTOCOL, HOSTNAME, "/data/users/avatar/").toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
