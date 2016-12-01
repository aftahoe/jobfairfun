package job.fair.jobfair.jpa.service;

import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Question;
import job.fair.jobfair.jpa.entity.Resume;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.scheduler.Scheduler;
import job.fair.jobfair.scheduler.SchedulerFactory;
import job.fair.jobfair.wsclient.WSClient;
import job.fair.tutormeet.TutorMeet;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_ACTION;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERID;

/**
 * Created by annawang on 1/26/16.
 */
@Service
public class CandidateServiceImpl implements CandidateService {

    static final Logger logger = Logger.getLogger(CandidateServiceImpl.class);
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private CompanyRepository companyRepository;

    //TODO remove
    private TutorMeet tutormeet;

    //TODO remove
    public CandidateServiceImpl() {
        tutormeet = new TutorMeet();
    }

    public CandidateServiceImpl(TutorMeet tutormeet, CandidateRepository repository) {
        this.candidateRepository = repository;
        this.tutormeet = tutormeet;
    }

    //
//    /**
//     * Check the candidate email to see if the user already exists or not.
//     *
//     * @param candidate
//     * @return true if the user exists or false if the user not exists
//     */
    @Override
    public boolean isExistingCandidate(Candidate candidate) {
        return candidateRepository.isExistingCandidate(candidate.getEmail());
    }

    //    /**
//     * Save the new <code>Candidate</code> to the database.
//     *
//     * @param candidate
//     * @return candidate Id in DB
//     */
    @Override
    public long addCandidate(Candidate candidate) {

        return candidateRepository.addCandidate(candidate).getId();
    }

    //    /**
//     * Get the candidate by the email.
//     *
//     * @param email
//     * @return the <code>Candidate</code> if found.
//     * null if not found.
//     */
    @Override
    public Candidate getCandidateByEmail(String email) {
        return candidateRepository.getCandidateByEmail(email);
    }

    @Override
    public Candidate getCandidate(String uuid) {
        return candidateRepository.getCandidate(uuid);
    }

    @Override
    public Candidate getCandidate(long id) {
        return candidateRepository.getCandidate(id);
    }

    @Override
    public Candidate updateCandidateStatus(Candidate candidate) {
        Candidate updatedCandidate = this.candidateRepository
                .updateCandidateStatus(candidate.getId(), candidate.getStatus());

        // publish the message is needed
        if (updatedCandidate.getJobqueue() != null && !updatedCandidate.getJobqueue().isEmpty()) {
            long joblongid = Long.parseLong(updatedCandidate.getJobqueue().substring(8));
            WSClient.publishStatusChange(updatedCandidate, companyRepository.getJob(joblongid));
        }
        return updatedCandidate;
    }


    @Override
    public List<Candidate> removeUserFromQueue(Job job, long userid, String userUUID, User.Type type) {
        logger.debug("want to remove " + userid + " from jobqueue " + job.getId());
        Scheduler scheduler = SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue" + job.getId());
        logger.debug("############ there is " + scheduler.getJobsWaiting().size() + " waiting");
        scheduler.removeFromWaitingQueue(userid, job.getUuid(), job.getCompany().getName(), job.getPositionName());
        logger.debug("############ there is " + scheduler.getJobsWaiting().size() + " waiting after removed");

        List<Candidate> remainCandidates = new LinkedList<>();
        Collection<Candidate> waitingCandidates = scheduler.getJobsWaiting();
        for (Candidate c : waitingCandidates) {
            remainCandidates.add(c);
        }
        this.candidateRepository.removeCandidateJobQueue(userid);

        //publish
        JSONObject jobWSMessage = new JSONObject();
        jobWSMessage.put(WS_MESSAGE_ACTION, WS_MESSAGE_ACTION_REMOVE);
        jobWSMessage.put(WS_MESSAGE_DATA, new JSONObject()
                .put(WS_MESSAGE_DATA_USERID, userUUID)
                .put(WS_MESSAGE_DATA_REMOVEBY, type.name()));
        WSClient.singleton().client.publish(WSClient.JOBWSPREFIX + job.getUuid(), jobWSMessage.toString());
        JSONObject userWSMessage = new JSONObject();
        userWSMessage.put(WS_MESSAGE_ACTION, WS_MESSAGE_ACTION_REMOVE);
        userWSMessage.put(WS_MESSAGE_DATA, new JSONObject()
                .put(WS_MESSAGE_DATA_JOBID, job.getUuid())
                .put(WS_MESSAGE_DATA_REMOVEBY, type.name())
                .put(WS_MESSAGE_DATA_COMPANYNAME, job.getCompany().getName())
                .put(WS_MESSAGE_DATA_POSITIONNAME, job.getPositionName()));
        WSClient.singleton().client.publish(WSClient.USERWSPREFIX + userUUID, userWSMessage.toString());

        return remainCandidates;
    }

    @Override
    public Candidate.CandidateStatus getCandidateStatus(long id) {
        return this.candidateRepository.getCandidateStatus(id);
    }


    //    /**
//     * Sign up from TutorMeet web service.
//     *
//     * @param candidate
//     * @throws IOException
//     * @throws URISyntaxException
//     * @throws CandidateException when sign up failed on TutorMeet web service.
//     */
    @Override
    public void signup(Candidate candidate) throws IOException, URISyntaxException, CandidateException {
        JSONObject candidateObject = tutormeet.signup(
                candidate.getEmail(), candidate.getName(), candidate.getPassword(), candidate.getLocale());
        if ((int) candidateObject.get("returnCode") != 0) {
            throw new CandidateException((String) candidateObject.get("message"));
        }
    }

    //    /**
//     * Apply job for the candidate.
//     * 1. Save the candidate resume
//     * 2. update candidate information in DB
//     * 3. Add candidate to Job Queue
//     * 4. Convert candidate resume
//     *
//     * @param candidate
//     * @param resumes
//     * @return
//     * @throws IOException
//     */
    @Override
    public Application applyJob(Job job, User user, Application application, List<MultipartFile> resumes) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        File dir = new File("/tmp/" + Long.toString(user.getUserId()));  //TODO change the directory
        if (!dir.exists()) {
            boolean result = false;
            try {
                dir.mkdir();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (result) {
                System.out.println("DIR created");
            }
        }

        // 1. Save the resume
        String fileName = "";
        for (MultipartFile file : resumes) {
            fileName = dir + File.separator + file.getOriginalFilename();
            file.transferTo(new File(fileName));
        }
        // 2. update candidate information
        //candidate.setResume(fileName);
        Candidate candidate = new Candidate();
        candidate.setEmail(user.getEmail());
        candidate.setJobqueue("jobqueue" + job.getId());
        //candidate.addApplication(application);
        //TODO LazyInitalizationException
        //candidate.addScore(job.getId(), candidate.getJobscore());


        // 5. convert user resume
        Candidate updatedCanddate = candidateRepository.updateCandidate(candidate.getEmail(), candidate, true);
        if (updatedCanddate.getId() == 0) {
            application.setCandidate(updatedCanddate);
            return application;
        }
        application.setCandidate(updatedCanddate);
        candidateRepository.addApplication(application);
        //updatedCanddate.setJobscore(candidate.getJobscore());

        //  add the candidate to queue
        Scheduler scheduler = SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue" + job.getId());
        int queuePosition = scheduler.submit(updatedCanddate);
        application.setQueuePosition(queuePosition);
        application.setCandidate(updatedCanddate);
        return application;
    }

    @Override
    public Application getApplication(long candidateId, long jobId) {
        ArrayList<Application> applications = this.candidateRepository.getApplications(candidateId, jobId);
        Application application = applications.get(0);
        application.getCandidate().setIconPathPrefix(TutorMeet.getAvatarPrefix(application.getCandidate().getTutorMeetUserSN()));

        return application;
    }

    @Override
    public Application getLatestApplication(long candidateId) {
        ArrayList<Application> applications = this.candidateRepository.getApplications(candidateId);

        if (applications.isEmpty()) return null;

        Application application = applications.get(0);
        application.setScore(-1);
        application.getCandidate().setAvatar(null);
        return application;
    }

    @Override
    public Candidate wipeOutForCandidate(Candidate c) {
        c.setPassword("");
        c.setTutorMeetToken("");
        c.setTutorMeetApitoken("");
        c.setTutorMeetUserSN(0);
        // c.setResume("");
        c.setAvatar("");
        c.setIconPathPrefix("");
        return c;
    }

    @Override
    public Candidate updateCandidateProfile(String candidateEmail, Candidate candidate) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Candidate c = candidateRepository.updateCandidate(candidateEmail, candidate, false);
        wipeOutForCandidate(c);
        return c;
    }

    @Override
    public float calculateJobTestScore(Job job, JSONArray answers) {
        List<Question> questioins = job.getQuestionSet();
        int questionSize = questioins.size();
        int correctCount = 0;


        Map<String, Short> answerMap = new HashMap<>();
        for (int i = 0; i < answers.length(); i++) {
            JSONObject answerObject = (JSONObject) answers.get(i);
            answerMap.put(answerObject.getString("questionId"), (short) answerObject.getInt("answer"));
        }


        for (Question q : questioins) {
            Short candidateAnswer = answerMap.get(q.getUuid());
            if (candidateAnswer != null) {
                if (candidateAnswer.shortValue() == q.getAnswer()) {
                    correctCount++;
                }
            } else {
                throw new IllegalArgumentException("Cannot find answer for Question " + q.getUuid());
            }
        }
        float score = (float) correctCount / (float) questionSize;
        // put the score into DB
        return score;

    }

    @Override
    public Collection<Job> putFavoriteJob(long candidateID, Job job) {
        return this.candidateRepository.addFavoriteJob(candidateID, job);
    }

    @Override
    public Collection<Job> getFavoriteJobs(long candidateID) {
        return this.candidateRepository.getFavoriteJob(candidateID);
    }

    @Override
    public Collection<Job> deleteFavoriteJob(long candidateID, String jobUUID) {
        return this.candidateRepository.deleteFavoriteJob(candidateID, jobUUID);
    }

    @Override
    public Resume addResume(Resume resume) {
        return this.candidateRepository.addResume(resume);
    }

    //for test
    public CandidateRepository getCandidateRepository() {
        return candidateRepository;
    }
}
