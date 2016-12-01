package job.fair.jobfair.jpa.controller;

import job.fair.commons.ObjectConverter;
import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.JobfairConstants;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Resume;
import job.fair.jobfair.jpa.entity.Status;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.service.CandidateService;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.jpa.service.UserService;
import job.fair.jobfair.wsclient.WSClient;
import job.fair.tutormeet.Exception.TutorMeetException;
import job.fair.tutormeet.TutorMeet;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static job.fair.commons.ArgumentCheckUtils.require;
import static job.fair.commons.ArgumentCheckUtils.requireNonEmptyValue;

/**
 * Created by annawang on 1/26/16.
 */
@RestController
@RequestMapping("/candidates")
public class CandidateController {

    static final Logger logger = Logger.getLogger(CompanyController.class);
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private MessageSource messageSource;

    //constructors
    public CandidateController(CandidateService candidateService, UserService userService, HttpServletRequest request) {
        this.candidateService = candidateService;
        this.userService = userService;
        this.request = request;
    }

    public CandidateController() {
    }


    /**
     * The candidate logins to the JobFair server.
     *
     * @param candidate login candidate
     * @return login JSONObject String with following keys:
     * {"id", "iconPathPrefix", "name", "avatar", "email", "role"}
     * @throws IllegalAccessException    DataBase error
     * @throws CandidateException        Login with UNKNOWN user type. (Type should be either Canidate or Interviewer)
     * @throws IOException               TutorMeet Login Error
     * @throws URISyntaxException        TutorMeet Login Error
     * @throws NoSuchMethodException     DataBase error
     * @throws InvocationTargetException DataBase error
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String candidateLogin(@RequestBody Candidate candidate) throws
            IllegalAccessException, CandidateException, IOException,
            URISyntaxException, NoSuchMethodException, InvocationTargetException {
        String candidateEmail = candidate.getEmail();
        String candidatePassword = candidate.getPassword();
        logger.info("User login request with email: " + candidateEmail + " password: " + candidatePassword);

        // email and password are required
        requireNonEmptyValue("Candidate's email", candidateEmail);
        requireNonEmptyValue("Candidate's password", candidatePassword);

        String loginObject = userService.login(candidateEmail, candidatePassword, request, User.Type.CANDIDATE).toString();
        return loginObject;
    }

    /**
     * The candidate logouts the server
     *
     * @return message of logout success if there is no error. Invalid session if there is no session
     * @throws Exception any possible problem
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Status candidateLogout() throws Exception {
        User candidateUser = this.userService.validateUserSession(request, User.Type.CANDIDATE);

        // Set user status to AWAY
        Candidate candidate = new Candidate();
        candidate.setId(candidateUser.getUserId());
        candidate.setUuid(candidateUser.getUseruuid());
        candidate.setStatus(Candidate.CandidateStatus.AWAY);
        this.candidateService.updateCandidateStatus(candidate);
        // Invalidate user session
        candidateUser.getUserSesson().invalidate();

        return new Status("candidate/logout", "logout success!");
    }


    /**
     * The candidate sign up to the JobFair server.
     *
     * @param candidate sign up candidate
     * @return Successful message if there is no exception
     * @throws Exception any possible problem
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Status candidateSignup(@RequestBody Candidate candidate) throws Exception {
        requireNonEmptyValue("Candidate's email", candidate.getEmail());
        requireNonEmptyValue("Candidate's password", candidate.getPassword());
        requireNonEmptyValue("Candidate's name", candidate.getName());
        requireNonEmptyValue("Candidate's locale", candidate.getLocale());

        candidateService.signup(candidate);

        return new Status("/candidate/signup", "Sent verification email to user");
    }

    /**
     * The candidate changes his/her status
     *
     * @param candidate the candidate who wants to change the status
     * @return current candidate profile
     * @throws Exception any possible Exception
     */
    @RequestMapping(value = "/status", method = RequestMethod.PUT)
    public
    @ResponseBody
    Candidate changeStatus(@RequestBody Candidate candidate) throws Exception {
        require("Candidate's status", candidate.getStatus());

        User candidateUser = this.userService.validateUserSession(request, User.Type.CANDIDATE);

        candidate.setId(candidateUser.getUserId());
        candidate.setUuid(candidateUser.getUseruuid());
        Candidate updatedCandidate = candidateService.updateCandidateStatus(candidate);

        return candidateService.wipeOutForCandidate(updatedCandidate);
    }

    /**
     * The candidate gets current status
     *
     * @return Current Candidate Status in JSONSTring: {status:0}
     * @throws Exception Any possible Exception
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public
    @ResponseBody
    String getStatus() throws Exception {
        long candidateId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();

        Candidate.CandidateStatus status = candidateService.getCandidateStatus(candidateId);

        return new JSONObject().put("status", status.ordinal()).toString();
    }


    /**
     * The candidate gets his/her latest application.
     *
     * @return candidate latest application in JSON
     * @throws Exception when validation fail
     */
    @RequestMapping(value = "/latestApplication", method = RequestMethod.GET)
    public
    @ResponseBody
    Application getCandidateLatestApplication() throws Exception {
        long candidateId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();

        return candidateService.getLatestApplication(candidateId);
    }

    /**
     * The candidate updates his/her profile
     *
     * @param candidate updated candidate information
     * @return Updated Candidate Profile
     * @throws Exception Any possible exception
     */
    @RequestMapping(value = "/profile", method = RequestMethod.PUT)
    public
    @ResponseBody
    Candidate updateCandidateProfile(@RequestBody Candidate candidate) throws Exception {
        String candidateEmail = this.userService.validateUserSession(request, User.Type.CANDIDATE).getEmail();

        return candidateService.updateCandidateProfile(candidateEmail, candidate);
    }

    /**
     * The candidate gets his/her favorite jobs
     *
     * @return Job list. Empty list is there is no favorite job
     * @throws Exception Any possible Exception
     */
    @RequestMapping(value = "/favoritejobs", method = RequestMethod.GET)
    public
    @ResponseBody
    Collection<Job> getCandidateFavoriteJobs() throws Exception {
        User candidate = this.userService.validateUserSession(request, User.Type.CANDIDATE);

        return candidateService.getFavoriteJobs(candidate.getUserId());
    }

    /**
     * The candidate add his/her favorite job
     *
     * @param jobID Job uuid
     * @return All favorite jobs
     * @throws Exception Any possible exception
     */
    @RequestMapping(value = "/favoritejobs/{jobID}", method = RequestMethod.PUT)
    public
    @ResponseBody
    Collection<Job> putCandidateFavoriteJobs(@PathVariable String jobID) throws Exception {
        User candidate = this.userService.validateUserSession(request, User.Type.CANDIDATE);
        Job job = this.companyService.getJob(jobID);

        if (job == null) {
            throw new IllegalArgumentException("Invalid Job ID: " + jobID);
        }
        return candidateService.putFavoriteJob(candidate.getUserId(), job);
    }

    /**
     * The candidate deletes one of his/her favorite jobs
     *
     * @param jobID Job UUID
     * @return Remaining favorite Jobs. Empty list if there is no more favorite jobs
     * @throws Exception Any possible exception
     */
    @RequestMapping(value = "/favoritejobs/{jobID}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<Job> deleteFavoriteJobs(@PathVariable String jobID) throws Exception {
        User candidate = this.userService.validateUserSession(request, User.Type.CANDIDATE);

        return candidateService.deleteFavoriteJob(candidate.getUserId(), jobID);
    }

    /**
     * The candidate applies a job (jobid) with his resume and details information.
     * <p>
     * Example:
     * headder: Content-type = application/json
     * <p>
     * POST body (form-data):   resume    - multipartfile
     * candidate - {"id": 1,"introduction":"my intro","workethic":"my workethic",
     * "cellphone":"my cell","wechatid":"my we chat","experience":4,
     * "salary":100000,"skills": ["JAVA", "HTML"]}
     *
     * @param jobID           applied job UUID
     * @param applicationString candidate applying information
     * @param resumes           candidate resume
     * @param answers           job tests answers from candidate
     * @return applied job information
     * @throws Exception Any possible Exception
     */
    //TODO add more tests
    @RequestMapping(value = "/application/{jobID}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String candidateApply(@PathVariable String jobID,
                                 @RequestParam(value = "questionAnswers", defaultValue = "[]") String answers,
                                 @RequestParam(value = "candidate", required = true) String applicationString,
                                 @RequestParam(value = "resume", required = true) List<MultipartFile> resumes)
            throws Exception {
        // Make sure the candidate information is valid
        User candidateUser = this.userService.validateUserSession(request, User.Type.CANDIDATE);
        Application application = validateApplicationJSON(applicationString);
        //candidate.setEmail(candidateUser.getEmail());
        //TODO test score = -1 is not test

        // Make sure the jobid is valid
        Job job = companyService.getJob(jobID);
        if (job == null) {
            throw new IllegalArgumentException("Invalid Job ID: " + jobID);
        }
        application.setJob(job);

        float score = getScoreByAnswers(answers, job);
        application.setScore(score);
        logger.debug("Candidate is applying for job with id " + job.getId() + " with score " + score);

        // Make sure the resume is there
        if (resumes.size() == 0) {
            throw new IllegalArgumentException("resume file is required and cannot be empty.");
        }

        //TODO: for now, user can only apply for 1 job before interviewing
        Application appliedCandidate = candidateService.applyJob(job, candidateUser, application, resumes);
        if (appliedCandidate.getCandidate().getId() == 0) {
            throw new IllegalArgumentException("You already applied job: " +
                    appliedCandidate.getCandidate().getJobqueue().substring(8) + ".");
        }

        //publish

        appliedCandidate.getCandidate().setIconPathPrefix(TutorMeet.getAvatarPrefix(appliedCandidate.getCandidate().getTutorMeetUserSN()));

        JSONObject jobWSMessage = new JSONObject();
        jobWSMessage.put(JobfairConstants.WSMessages.WS_MESSAGE_ACTION, JobfairConstants.WSMessages.WS_MESSAGE_ACTION_ADD);
        jobWSMessage.put(JobfairConstants.WSMessages.WS_MESSAGE_DATA, new JSONObject()
                .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERENTRY, ObjectConverter.toJSONObject(appliedCandidate)));
        WSClient.singleton().client.publish(WSClient.JOBWSPREFIX + jobID, jobWSMessage.toString());

        JSONObject userWSMessage = new JSONObject();
        userWSMessage.put(JobfairConstants.WSMessages.WS_MESSAGE_ACTION, JobfairConstants.WSMessages.WS_MESSAGE_ACTION_ADD);
        userWSMessage.put(JobfairConstants.WSMessages.WS_MESSAGE_DATA,
                new JSONObject()
                        .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITION, appliedCandidate.getQueuePosition())
                        .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID, jobID)
                        .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME, job.getCompany().getName())
                        .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME, job.getPositionName()));
        WSClient.singleton().client.publish(WSClient.USERWSPREFIX + candidateUser.getUseruuid(), userWSMessage.toString());
        // return job information

        return this.companyService.generateJobJSONObjectForCandidate(job).toString();
    }


    /**
     * The candidate deletes the applied job
     *
     * @param jobID applied job that wants to be deleted
     * @return Successful message if the removal is done
     * @throws Exception Any possible exception
     */
    @RequestMapping(value = "/application/{jobID}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Status deleteApply(@PathVariable String jobID) throws Exception {
        User candidateUser = this.userService.validateUserSession(request, User.Type.CANDIDATE);

        Job job = this.companyService.getJob(jobID);
        if (job == null) {
            throw new IllegalArgumentException("Invalid Job ID: " + jobID);
        }

        candidateService.removeUserFromQueue(job, candidateUser.getUserId(), candidateUser.getUseruuid(), User.Type.CANDIDATE);

        return new Status("DELETE /candidate/application", "Delete the user from the job queue " + jobID);
    }

    /**
     * The candidate gets all applied jobs
     *
     * @return all jobs in JSONArray with following JSONObject key:
     * jobuuid, companyName, positionName and queuePosition
     * @throws Exception Any possible Exception
     */
    @RequestMapping(value = "/application", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getApply() throws Exception {
        long candidateId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();
        Candidate candidate = candidateService.getCandidate(candidateId);

        JSONArray appliedJobs = new JSONArray();
        // return empty json array if the candidate didn't apply any job
        if (candidate.getJobqueue() == null || candidate.getJobqueue().isEmpty()) {
            return appliedJobs.toString();
        }

        long jobId = Long.parseLong(candidate.getJobqueue().substring(8));
        Job job = this.companyService.getJob(jobId);

        return appliedJobs.put(this.companyService.generateWaitingJobJSON(candidate, job)).toString();
    }

    @RequestMapping(value = "/jobs/interviewed", method = RequestMethod.GET)
    public
    @ResponseBody
    String getCandidateInterveiwHistory(@RequestParam(value = "startTime", required = true) Long startTime,
                                        @RequestParam(value = "endTime", required = true) Long endTime,
                                        @RequestParam(value = "countOnly", required = false, defaultValue = "false") Boolean countOnly)
            throws Exception {
        long candidateId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();

        logger.info("Interviewed Time between long value " + startTime.longValue() + " and " + endTime.longValue());
        Timestamp startTimeStamp = new Timestamp(startTime.longValue());
        Timestamp endTimeStamp = new Timestamp(endTime.longValue());
        logger.info("Interviewed Time between " + startTimeStamp.toString() + " and " + endTimeStamp.toString());
        List<Job> jobs = userService.getInterviewedJobs(candidateId, User.Type.CANDIDATE, startTimeStamp, endTimeStamp);
        if (countOnly) return new JSONObject().put("count", jobs.size()).toString();
        return ObjectConverter.toJSONString(jobs);
    }

    @RequestMapping(value = "/jobs/recommendation", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Collection<Job> searchRecommededCompanyJobs() throws Exception {
        long candidateId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();

        return this.companyService.searchRecommendedJobs(candidateId);
    }

    @RequestMapping(value = "/resume", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    Resume updateResume(@RequestBody Resume resume) throws Exception {
        //TODO do we need to verify the user?
        //long candidateId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();

        return this.candidateService.addResume(resume);
    }


    //TODO test this
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    private String handleDB(HttpServletRequest req, Exception e) {
        return e.getMessage();
    }

    //TODO test this
    @ExceptionHandler(TutorMeetException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private String handleTutorMeetInternalError(HttpServletRequest req, Exception e) {
        return e.getMessage();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    private Status handleTypeMismatchException(HttpServletRequest req, Exception e) {
        return new Status(req.getRequestURL().toString(), e.getMessage());
    }

    @ExceptionHandler(CandidateException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    private Status CandidateException(HttpServletRequest req, Exception e) {
        String errorURL = req.getRequestURL().toString();

        return new Status(errorURL, e.getMessage());
    }

    private Application validateApplicationJSON(String applicationJsonString) {
        JSONObject applicationJSON = new JSONObject(applicationJsonString);

        //Create Candidate object according to the json string
        Application application = new Application();
        application.setIntroduction(
                requireNonEmptyValue("Candidate's introduction", applicationJSON.getString("introduction")));
        application.setWorkethic(
                requireNonEmptyValue("Candidate's workethic", applicationJSON.getString("workethic")));

        // cellphone and wechatid are not required
        application.setCellphone(applicationJSON.isNull("cellphone") ? "" : applicationJSON.getString("cellphone"));
        application.setWechatid(applicationJSON.isNull("wechatid") ? "" : applicationJSON.getString("wechatid"));

        JSONArray skillsJSONArray = null;
        try {
            skillsJSONArray = (JSONArray) applicationJSON.get("skills");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid skills JSONArray");
        }
        if (skillsJSONArray.length() == 0) {
            throw new IllegalArgumentException("Skill JSONArray is required and cannot be empty");
        }
        Set<String> skills = new HashSet<>();
        for (int i = 0; i < skillsJSONArray.length(); i++) {
            skills.add(skillsJSONArray.getString(i));
        }
        application.setSkills(skills);

        application.setExperience(
                require("Candidate's experience", applicationJSON.getInt("experience")));
        application.setSalary(
                require("Candidate's salary", applicationJSON.getInt("salary")));

        return application;
    }

    private float getScoreByAnswers(String answers, Job job) {
        //Make sure we have enough answers
        JSONArray answersJSONArray = new JSONArray(answers);
        if (job.getQuestionSet().size() != answersJSONArray.length()) {
            throw new IllegalArgumentException(
                    job.getQuestionSet().size() + " answers are expected but there are " + answersJSONArray.length() + " got!");
        }
        //Calculate the test score
        float score = (float) -1;
        if (job.getQuestionSet().size() > 0) {
            score = candidateService.calculateJobTestScore(job, answersJSONArray);
        }
        return score;
    }
}
