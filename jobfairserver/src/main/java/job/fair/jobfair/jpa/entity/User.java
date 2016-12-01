package job.fair.jobfair.jpa.entity;

import org.json.JSONObject;

import javax.servlet.http.HttpSession;

import static job.fair.jobfair.JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE;

/**
 * Created by annawang on 2/19/16.
 */
public class User {
    private Type userType;
    private long userId;
    private String useruuid;
    private long companyId;
    private HttpSession userSesson;
    private int tutormeetUserSn;
    private String avatar;

    private String email;
    private String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUseruuid() {
        return useruuid;
    }

    public void setUseruuid(String useruuid) {
        this.useruuid = useruuid;
    }

    public int getTutormeetUserSn() {
        return tutormeetUserSn;
    }

    public void setTutormeetUserSn(int tutormeetUserSn) {
        this.tutormeetUserSn = tutormeetUserSn;
    }

    public Type getUserType() {
        return userType;
    }

    public void setUserType(Type userType) {
        this.userType = userType;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public JSONObject getUserSessonObject() {
        return new JSONObject((String) userSesson.getAttribute(SESSION_USER_ATTRIBUTE));
    }

    public HttpSession getUserSesson() {
        return this.userSesson;
    }

    public void setUserSesson(HttpSession userSesson) {
        this.userSesson = userSesson;
    }

    public static enum Type {
        CANDIDATE,
        INTERVIEWER,
        INTERVIEW, // This is for distinguishing between removing users is because of after interview
        UNKNOWN;
    }
}
