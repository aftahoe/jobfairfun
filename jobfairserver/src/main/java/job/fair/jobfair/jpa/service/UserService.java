package job.fair.jobfair.jpa.service;

import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.User;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by annawang on 2/16/16.
 */
public interface UserService {


    JSONObject login(String email, String password, HttpServletRequest request, User.Type userType)
            throws IOException, URISyntaxException, CandidateException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    User validateUserSession(HttpServletRequest requeest, User.Type userType) throws Exception;

    JSONObject generateLoginUserObject(JSONObject tutormeetLoginObject, User.Type usertype);

    List<Job> getInterviewedJobs(long userID, User.Type userType, Timestamp start, Timestamp end);
}
