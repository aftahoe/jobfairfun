package job.fair.jobfair.jpa.controller;

import job.fair.commons.ArgumentCheckUtils;
import job.fair.commons.ObjectConverter;
import job.fair.data.Page;
import job.fair.jobfair.Exception.JobfairException;
import job.fair.jobfair.JobfairConstants;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Status;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.service.CandidateService;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.jpa.service.InterviewerService;
import job.fair.jobfair.jpa.service.UserService;
import job.fair.jobfair.scheduler.SchedulerFactory;
import job.fair.jobfair.wsclient.WSClient;
import job.fair.tutormeet.TutorMeet;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sun.misc.BASE64Encoder;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by annawang on 2/15/16.
 */
@Controller
@RequestMapping("/interviewers")
public class InterviewerController {
    static final Logger logger = Logger.getLogger(InterviewerController.class);

    @Autowired
    private CompanyService companyService;
    @Autowired
    private InterviewerService interviewerService;
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private MessageSource messageSource;

    /**
     * Interviewer logins to jobfair server
     *
     * @param interviewer interviewer with email and password
     * @return login JSONObject String with following keys:
     * {"id", "iconPathPrefix", "name", "avatar", "email", "role"}
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String login(@RequestBody Interviewer interviewer) throws Exception {
        logger.debug("interviewer login information " + interviewer.getEmail() + " password " + interviewer.getPassword());
        ArgumentCheckUtils.requireNonEmptyValue("Interviewer's email", interviewer.getEmail());
        ArgumentCheckUtils.requireNonEmptyValue("Interviewer's password", interviewer.getPassword());

        return userService.login(interviewer.getEmail(), interviewer.getPassword(), request, User.Type.INTERVIEWER).toString();
    }

    /**
     * The interviewer logouts the server
     *
     * @return message of logout success if there is no error. Invalid session if there is no session
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Status logout() throws Exception {
        User interviewerUser = this.userService.validateUserSession(request, User.Type.INTERVIEWER);

        // Invalidate user session
        interviewerUser.getUserSesson().invalidate();

        return new Status("candidate/logout", "logout success!");
    }

    /**
     * Get all jobs belong to the interviewer's company
     *
     * @return list of jobs
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    public
    @ResponseBody
    Page getJobs(@RequestParam(defaultValue = "0") int page,
                 @RequestParam(defaultValue = "0") int size) throws Exception {
        User interviewerUser = this.userService.validateUserSession(request, User.Type.INTERVIEWER);
        long companyId = interviewerUser.getCompanyId();
        long interviewerId = interviewerUser.getUserId();

        Pageable pageable;
        if (size == 0) {
            pageable = null;
        } else {
            pageable = new PageRequest(page, size);
        }
        return interviewerService.getInterviewerJobs(companyId, interviewerId, pageable);
    }

    @RequestMapping(value = "/candidates/interviewed", method = RequestMethod.GET)
    public
    @ResponseBody
    String getCandidateInterveiwHistory(@RequestParam(value = "startTime", required = true) Long startTime,
                                        @RequestParam(value = "endTime", required = true) Long endTime,
                                        @RequestParam(value = "countOnly", required = false, defaultValue = "false") Boolean countOnly)
            throws Exception {
        long interviewerId = this.userService.validateUserSession(request, User.Type.CANDIDATE).getUserId();

        Timestamp startTimeStamp = new Timestamp(startTime.longValue());
        Timestamp endTimeStamp = new Timestamp(endTime.longValue());
        List<Candidate> candidates = interviewerService.getInterviewedCandidates(interviewerId, startTimeStamp, endTimeStamp);
        if (countOnly) return new JSONObject().put("count", candidates.size()).toString();
        return ObjectConverter.toJSONString(candidates);
    }

    /**
     * Get all applied candidate under the job
     *
     * @param jobID uuid of the job
     * @return list of candidates
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/job/{jobID}/candidates", method = RequestMethod.GET)
    public
    @ResponseBody
    LinkedList<Application> getJobCandidates(@PathVariable String jobID) throws Exception {
        long companyId = this.userService.validateUserSession(request, User.Type.INTERVIEWER).getCompanyId();
        // make sure the jobid is belong to interviewer's company
        Job job = validateJob(jobID, companyId);

        long jobId = job.getId();
        LinkedList<Candidate> waitingCandidates = SchedulerFactory.singleton()
                .createOrGetFIFOScheduler("jobqueue" + jobId).getJobsWaiting();
        LinkedList<Application> candidates = new LinkedList<>();
        for (Candidate c : waitingCandidates) {
            //get candidate from DB
            Application application = this.candidateService.getApplication(c.getId(), jobId);

            candidates.add(application);
        }
        return candidates;
    }

    /**
     * Interviewer gets the evaluation questions according to his/her company
     *
     * @return list of evaluation questions
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/evaluation", method = RequestMethod.GET)
    public
    @ResponseBody
    Collection<EvaluationQuestion> getEvaluationQuestions() throws Exception {
        long companyId = this.userService.validateUserSession(request, User.Type.INTERVIEWER).getCompanyId();

        return this.companyService.getEvaluationQuestions(companyId);
    }

    /**
     * Interviewer posts the evaluation report
     *
     * @param report
     * @return successful message if report gets post
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/evaluation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Status postEvaluationReport(@RequestBody EvaluationReport report) throws Exception {//TODO make sure the scores are valid
        User interviewerUser = this.userService.validateUserSession(request, User.Type.INTERVIEWER);

        ArgumentCheckUtils.requireNonEmptyValue("Evaluation Report's Note ", report.getNote());
        ArgumentCheckUtils.requireNonEmptyValue("Evaluation Report's Candidate ID ", report.getCandidateId());
        ArgumentCheckUtils.requireNonEmptyValue("Evaluation Report's Job ID ", report.getJobId());

        Job job = validateJob(report.getJobId(), interviewerUser.getCompanyId());

        Application application = validateAndGetCandidateApplication(report.getCandidateId(), job.getId());


        Interviewer interviewer = this.interviewerService.getInterviewer(interviewerUser.getUserId());
        report.setInterviewer(interviewer);
        report.setApplication(application);
        this.companyService.putEvaluationResult(report);

        this.candidateService.removeUserFromQueue(job, application.getCandidate().getId(),
                application.getCandidate().getUuid(), User.Type.INTERVIEW);

        application.getCandidate().setStatus(Candidate.CandidateStatus.AVAILABLE);
        this.candidateService.updateCandidateStatus(application.getCandidate());

        return new Status("POST /interviewer/evaluation", "Submit the evaluation result");
    }

    /**
     * Interviewer launches interview with the candidate who applied for the job
     *
     * @param jobuuid       UUID of the job
     * @param candidateuuid UUID of the candidate
     * @return meeting host URL for the interviewer
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/launchinterview", params = {"jobid", "candidateid"}, method = RequestMethod.GET)
    public
    @ResponseBody
    String launchInterview(@RequestParam(value = "jobid") String jobuuid,
                           @RequestParam(value = "candidateid") String candidateuuid) throws Exception {
        User interviewerUser = this.userService.validateUserSession(request, User.Type.INTERVIEWER);

        // Make sure job id is valid
        Job job = validateJob(jobuuid, interviewerUser.getCompanyId());

        // Make sure the candidate is available for interview
        Candidate candidate = this.candidateService.getCandidate(candidateuuid);
        if (candidate == null) {
            throw new IllegalArgumentException("Invalid Candidate ID: " + candidateuuid);
        }
        Candidate.CandidateStatus currentStatus = candidate.getStatus();
        if (!currentStatus.equals(Candidate.CandidateStatus.AVAILABLE)) {
            throw new JobfairException("Candidate " + candidateuuid + " is not available right now");
        }

        Interviewer in = this.interviewerService.updateOrGetInterviewerMeetingID(interviewerUser.getUserId());

        logger.info("launch the meeting with meeting id " + in.getMeetingID());
        // Calculate the URL //TODO put to the service layer?
        String hostMeetingUriStr = "-3|" + in.getTutorMeetUserSN() + "|" + in.getMeetingID() + "|" + 34;
        String hostMeetingURI = new BASE64Encoder().encode(hostMeetingUriStr.getBytes());

        String attendeeMeetingUriStr = "-3|" + candidate.getTutorMeetUserSN() + "|" + in.getMeetingID() + "|" + 36;
        String attendeeMeetingURI = new BASE64Encoder().encode(attendeeMeetingUriStr.getBytes());

        // Change candidate status
        candidate.setStatus(Candidate.CandidateStatus.INTERVIEW);
        this.candidateService.updateCandidateStatus(candidate);

        //publish
        JSONObject userWSMessage = new JSONObject();
        userWSMessage.put(JobfairConstants.WSMessages.WS_MESSAGE_ACTION, JobfairConstants.WSMessages.WS_MESSAGE_ACTION_INTERVIEW);
        userWSMessage.put(JobfairConstants.WSMessages.WS_MESSAGE_DATA, new JSONObject()
                .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID, jobuuid)
                .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME, job.getCompany().getName())
                .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME, job.getPositionName())
                .put(JobfairConstants.WSMessages.WS_MESSAGE_DATA_ATTENDEE_MEETINGURL, new TutorMeet().MEETING_URL_PREFIX + attendeeMeetingURI));
        WSClient.singleton().client.publish(WSClient.USERWSPREFIX + candidateuuid, userWSMessage.toString());


        return new JSONObject().put("hostMeetingURL", new TutorMeet().MEETING_URL_PREFIX + hostMeetingURI).toString();
    }

    /**
     * Interviewer removes candidate from his/her waiting job queue
     *
     * @param jobID       UUID of the job
     * @param candidateID UUID of the candidate
     * @return list of remaining candidates
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/job/{jobID}/candidates/{candidateID}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Collection<Candidate> removeJobCandidate(@PathVariable String jobID, @PathVariable String candidateID)
            throws Exception {
        User interviewerUser = this.userService.validateUserSession(request, User.Type.INTERVIEWER);

        Job job = validateJob(jobID, interviewerUser.getCompanyId());
        Candidate candidate = this.candidateService.getCandidate(candidateID);
        if (candidate == null) {
            throw new IllegalArgumentException("Invalid Candidate ID: " + candidateID);
        }

        //modify DB
        List<Candidate> remainCandidates =
                this.candidateService.removeUserFromQueue(job, candidate.getId(), candidateID, User.Type.INTERVIEWER);

        return remainCandidates;
    }

    /**
     * Interviewer gets a list of recommended candidates
     *
     * @return a list of evaluation reports
     * @throws Exception any possible exception
     */
    @RequestMapping(value = "/recommended/candidates", method = RequestMethod.GET)
    public
    @ResponseBody
    Page<EvaluationReport> getRecommendedCandidates(@RequestParam(value = "jobId", required = false, defaultValue = "") String jobId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "0") int size) throws Exception {
        long interviewerId = this.userService.validateUserSession(request, User.Type.INTERVIEWER).getUserId();

        Pageable pageable;
        if (size == 0) {
            pageable = null;
        } else {
            pageable = new PageRequest(page, size);
        }
        return this.interviewerService.getInterviewerInterviewJobs(interviewerId, jobId, pageable);
    }

    private Job validateJob(String jobUUID, long companyId) {
        Job job = this.companyService.getJob(jobUUID);
        if (job == null || job.getCompany().getId() != companyId) {
            throw new IllegalArgumentException("Invalid job ID: " + jobUUID + ". "
                    + "Please make sure job id is valid or you have the right to view this job.");
        }
        return job;
    }

    private Application validateAndGetCandidateApplication(String candidateUUID, long jobid) {
        Candidate candidate = this.candidateService.getCandidate(candidateUUID);

        if (candidate == null) {
            throw new IllegalArgumentException("Invalid Candidate ID: " + candidateUUID);
        }
        Application application = this.candidateService.getApplication(candidate.getId(), jobid);
        return application;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    private Status handleTypeMismatchException(HttpServletRequest req, Exception e) {
        return new Status(req.getRequestURL().toString(), e.getMessage());
    }
}
