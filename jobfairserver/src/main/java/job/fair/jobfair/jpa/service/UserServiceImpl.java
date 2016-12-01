package job.fair.jobfair.jpa.service;

import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.InterviewerRepository;
import job.fair.jobfair.wsclient.WSClient;
import job.fair.tutormeet.TutorMeet;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_COMPANY_ID;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_ROLE;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_USER_ID;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_USER_STATUS;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_USER_UUID;
import static job.fair.jobfair.JobfairConstants.SessionParms.SESSION_ID;
import static job.fair.jobfair.JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_APITOKEN;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_AVATAR;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_EMAIL;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_NAME;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_TOKEN;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_USERSN;


/**
 * Created by annawang on 2/16/16.
 */
@Service
public class UserServiceImpl implements UserService {
    static final Logger logger = Logger.getLogger(UserServiceImpl.class);
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private InterviewerRepository interviewerRepository;
    @Autowired
    private CandidateService candidateService;
    private TutorMeet tutormeet;


    // Constructors
    public UserServiceImpl() {
        tutormeet = new TutorMeet();
    }

    //For unit test
    public UserServiceImpl(TutorMeet tutormeet, CandidateRepository repository, CompanyRepository companyRepository, InterviewerRepository interviewerRepository) {
        this.companyRepository = companyRepository;
        this.interviewerRepository = interviewerRepository;
        this.candidateRepository = repository;
        this.tutormeet = tutormeet;
    }

    @Override
    public JSONObject login(String email, String password, HttpServletRequest request, User.Type userType)
            throws IOException, URISyntaxException, CandidateException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        logger.debug("login inside Interviewer Service");
        JSONObject tutormeetLoginObject = tutormeet.login(email, password);
        logger.debug("login inside Interviewer Service  -- back from tutormeet");
        if (tutormeetLoginObject != null) { // login success
            logger.debug("get a json object from tutormeet");
            if (tutormeetLoginObject.getInt("returnCode") == 0) {
                logger.debug("get a json object from tutormeet - with the return code 0");

                if (userType.equals(User.Type.INTERVIEWER)) {
                    JSONObject sessionObject = createOrUpdateInterviewer(email, password, request, tutormeetLoginObject);
                    return generateLoginUserObject(sessionObject, User.Type.INTERVIEWER);
                } else if (userType.equals(User.Type.CANDIDATE)) {
                    JSONObject sessionObject = createOrUpdateCandidate(email, password, request, tutormeetLoginObject);
                    return generateLoginUserObject(sessionObject, User.Type.CANDIDATE);
                } else {
                    throw new CandidateException("login with UNKNOWN user type");
                }
            } else {
                //TODO test
                logger.debug("get a json object from tutormeet - with the return code NOT 0");
                request.getSession().removeAttribute(SESSION_USER_ATTRIBUTE);
                throw new AuthenticationException("Invalid User Name and Password");
            }
        } else {
            logger.debug("login inside Candidate Service  didn't get anything from tutormeet");
            throw new AuthenticationException("Invalid User Name and Password");
        }
    }

    @Override
    public User validateUserSession(HttpServletRequest request, User.Type userType) throws Exception {
        logger.info("Validating user session");
        long useId;
        long companyId = -1;
        HttpSession session = request.getSession(false);
        User user = new User();
        if (session != null) { // From web client, check session id.
            logger.debug("This is user with session id " + session.getId());
            String userAttribue = (String) session.getAttribute(SESSION_USER_ATTRIBUTE);
            try {
                JSONObject userJson = new JSONObject(userAttribue);
                String sessionId = userJson.getString(SESSION_ID);
                useId = userJson.getLong(JOBFAIR_USER_ID);

                logger.debug("Jobfair user id is " + useId);
                if (!sessionId.equals(session.getId())) {
                    throw new AuthenticationException("Session Authentication Failed: Different Session ID");
                }

                if (User.Type.UNKNOWN.equals(userType)) {
                    if (userJson.isNull(JOBFAIR_COMPANY_ID)) {
                        user.setUserType(User.Type.CANDIDATE);
                    } else {
                        companyId = userJson.getLong(JOBFAIR_COMPANY_ID);
                        user.setUserType(User.Type.INTERVIEWER);
                    }
                }
                if (User.Type.INTERVIEWER.equals(userType)) {
                    if (userJson.isNull(JOBFAIR_COMPANY_ID)) {
                        throw new AuthenticationException("Please login as an interviewer");
                    }
                    companyId = userJson.getLong(JOBFAIR_COMPANY_ID);
                }
                user.setCompanyId(companyId);
                user.setUserId(useId);
                user.setUserSesson(session);
                user.setUseruuid(userJson.getString(JOBFAIR_USER_UUID));
                user.setEmail(userJson.getString(TUTORMEET_LOGINOBJECT_EMAIL));
                user.setTutormeetUserSn(userJson.getInt(TUTORMEET_LOGINOBJECT_USERSN));
                user.setAvatar(userJson.getString(TUTORMEET_LOGINOBJECT_AVATAR));
            } catch (JSONException je) {
                je.printStackTrace();
                throw new AuthenticationException("Session Authentication Failed: Unknown JSON Object");
            } catch (Exception e) {
                e.printStackTrace();
                throw new AuthenticationException(e.getMessage());
            }
        } else { // From mobile client, check token.
            throw new AuthenticationException("Session Authentication Failed: Session is Null");
        }
        return user;
    }


    @Override
    public List<Job> getInterviewedJobs(long userID, User.Type userType, Timestamp start, Timestamp end) {
        ArrayList<Job> jobs = null;
        if (userType.equals(User.Type.CANDIDATE)) {
            jobs = this.companyRepository.getCandidateInterviewedJobs(userID, start, end);
            logger.info("Candidate with id " + userID + " gets his/her interviewed jobs #" + jobs.size());
        } else if (userType.equals(User.Type.INTERVIEWER)) {
            jobs = this.companyRepository.getInterviewerInterviewedJobs(userID, start, end);
        }
        if (jobs == null || jobs.isEmpty()) return Collections.emptyList();

        return jobs;
    }

    private JSONObject createOrUpdateCandidate(
            String email, String password, HttpServletRequest request, JSONObject tutormeetLoginObject)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Candidate updatedCandidate = null;
        Candidate c = new Candidate();
        c.setName(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_NAME));
        c.setTutorMeetToken(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_TOKEN));
        c.setTutorMeetApitoken(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_APITOKEN));
        c.setTutorMeetUserSN(tutormeetLoginObject.getInt(TUTORMEET_LOGINOBJECT_USERSN));
        c.setStatus(Candidate.CandidateStatus.AVAILABLE);
        c.setEmail(email);
        c.setPassword(password);
        c.setAvatar(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_AVATAR));
        if (this.candidateRepository.isExistingCandidate(email)) {
            //update user info
            updatedCandidate = this.candidateRepository.updateCandidate(email, c, false);
        } else {
            // save all token information from tutormeet to DB
            updatedCandidate = this.candidateRepository.addCandidate(c);
        }
        JSONObject sessionObject = createSession(request, tutormeetLoginObject, updatedCandidate.getId(),
                updatedCandidate.getUuid(), updatedCandidate.getStatus(), -1);

        if (updatedCandidate.getJobqueue() != null && !updatedCandidate.getJobqueue().isEmpty()) {
            long joblongid = Long.parseLong(updatedCandidate.getJobqueue().substring(8));
            WSClient.publishStatusChange(updatedCandidate, companyRepository.getJob(joblongid));
        }
        return sessionObject;
    }

    private JSONObject createOrUpdateInterviewer(
            String email, String password, HttpServletRequest request, JSONObject tutormeetLoginObject)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Interviewer updatedInterviewer;
        Interviewer in = new Interviewer();
        in.setName(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_NAME));
        in.setTutorMeetToken(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_TOKEN));
        in.setTutorMeetApitoken(tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_APITOKEN));
        in.setTutorMeetUserSN(tutormeetLoginObject.getInt(TUTORMEET_LOGINOBJECT_USERSN));
        in.setEmail(email);
        in.setPassword(password);
        if (this.interviewerRepository.isExistingInterviewer(email)) {
            //update user info
            updatedInterviewer = this.interviewerRepository.updateInterviewer(email, in);
        } else {
            // save all token information from tutormeet to DB
            updatedInterviewer = this.interviewerRepository.addInterviewer(in);
        }
        String domain = email.substring(email.indexOf('@') + 1);

        //TODO I only need ID
        Company company = this.companyRepository.getCompanyByDomain(domain);
        long companyId = 0;
        if (company != null) {
            companyId = company.getId();
        }

        return createSession(request, tutormeetLoginObject, updatedInterviewer.getId(), updatedInterviewer.getUuid(), null, companyId);
    }

    @Override
    public JSONObject generateLoginUserObject(JSONObject tutormeetLoginObject, User.Type usertype) {
        JSONObject returnObject = new JSONObject();
        returnObject.put("id", tutormeetLoginObject.getString(JOBFAIR_USER_UUID));
        returnObject.put("iconPathPrefix", TutorMeet.getAvatarPrefix(tutormeetLoginObject.getInt(TUTORMEET_LOGINOBJECT_USERSN)));
        returnObject.put("name", tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_NAME));
        returnObject.put("avatar", tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_AVATAR));
        returnObject.put("email", tutormeetLoginObject.getString(TUTORMEET_LOGINOBJECT_EMAIL));
        returnObject.put("role", usertype.name());

        return returnObject;
    }

    private JSONObject createSession(HttpServletRequest request, JSONObject tutormeetLoginObject,
                                     long userId, String userUUID, Candidate.CandidateStatus userStatus, long companyId) {
        logger.debug("creating a new session for user " + userId);
        HttpSession session = request.getSession(true);
        if (!session.isNew()) {
            session.invalidate();
            session = request.getSession(true);
        }
        session.setMaxInactiveInterval(24 * 60 * 60);
        tutormeetLoginObject.put(SESSION_ID, session.getId());

        tutormeetLoginObject.put(JOBFAIR_USER_ID, userId);
        tutormeetLoginObject.put(JOBFAIR_USER_UUID, userUUID);
        // Add jobfairCompanyId information to session if user is an interviewer
        if (companyId != -1) {
            tutormeetLoginObject.put(JOBFAIR_COMPANY_ID, companyId);
            tutormeetLoginObject.put(JOBFAIR_ROLE, User.Type.INTERVIEWER);
        } else {
            tutormeetLoginObject.put(JOBFAIR_ROLE, User.Type.CANDIDATE);
        }

        if (userStatus != null) {
            tutormeetLoginObject.put(JOBFAIR_USER_STATUS, userStatus.getOrdinal());
        }
        session.setAttribute(SESSION_USER_ATTRIBUTE, tutormeetLoginObject.toString());

        // Remove the session id from tutormeet and put jobfair session id
        tutormeetLoginObject.remove(SESSION_ID);
        tutormeetLoginObject.put("token", session.getId());
        logger.debug("finish creating a new session for user " + userId);
        return tutormeetLoginObject;
    }

    public CompanyRepository getCompanyRepository() {
        return this.companyRepository;
    }

    public InterviewerRepository getInterviewerRepository() {
        return this.interviewerRepository;
    }
}
