package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.InterviewerRepository;
import job.fair.jobfair.scheduler.Scheduler;
import job.fair.jobfair.scheduler.SchedulerFactory;
import job.fair.tutormeet.TutorMeet;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by annawang on 3/2/16.
 */
@Service
public class InterviewerServiceImpl implements InterviewerService {
    static final Logger logger = Logger.getLogger(InterviewerServiceImpl.class);
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private InterviewerRepository interviewerRepository;

    //TODO remove
    @Override
    public Page getInterviewerJobs(long companyId, long interviewerId, Pageable pageable) {
        if (companyId == 0) return new Page(Collections.emptyList(), 0);
        Page<Job> jobs = companyRepository.getCompanyJobsBasicInfo(companyId, pageable);

        for (Job job : jobs) {
            Scheduler scheduler = SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue" + job.getId());
            job.setCandidateWaitingCount(scheduler.getJobsWaiting().size());
            job.setRecommendationCount(this.companyRepository.getRecommendationCount(job.getId(), interviewerId));
        }
        return jobs;
    }


    @Override
    public Interviewer getInterviewer(long interviewerId) {
        return this.interviewerRepository.getInterviewer(interviewerId);
    }

    @Override
    public Interviewer getInterviewerByEmail(String email) {
        return this.interviewerRepository.getInterviewerByEmail(email);
    }

    @Override
    public Interviewer updateOrGetInterviewerMeetingID(long interviewerID) throws IOException, URISyntaxException {
        Interviewer in = this.getInterviewer(interviewerID);
        if (in.getMeetingID() == null || in.getMeetingID().isEmpty()) {
            //call tutormeetAPI
            logger.info("@@@@@@@@@@@@@@@@@ There is no meeting id, needs to create one ");
            JSONObject meetingJSON = new TutorMeet().getMeetingID("", in.getTutorMeetApitoken());
            String meetingId = meetingJSON.getString("meetingId");
            logger.info("New meeting id " + meetingId);
            in.setMeetingID(meetingId);
            this.interviewerRepository.updateInterviewerMeetingID(interviewerID, meetingId);
        } else {
            logger.info("@@@@@@@@@@@@@@@@@@ User current meeting id " + in.getMeetingID());
            new TutorMeet().getMeetingID(in.getMeetingID(), in.getTutorMeetApitoken());
            //String meetingId = meetingJSON.getString("meetingId");
            //in.setMeetingID(meetingId);
            //this.interviewerRepository.updateInterviewerMeetingID(interviewerID, meetingId);
        }
        return in;
    }

    @Override
    public Page<EvaluationReport> getInterviewerInterviewJobs(long interviewerID, String jobId, Pageable pageable) {
        Page<EvaluationReport> reports;
        if (jobId.isEmpty()) {
            reports = this.companyRepository.getEvaluationReport(interviewerID, pageable);
        } else {
            reports = this.companyRepository.getEvaluationReport(interviewerID, jobId, pageable);
        }

        for (EvaluationReport e : reports) {

            e.getApplication().getCandidate().setIconPathPrefix(
                    TutorMeet.getAvatarPrefix(e.getApplication().getCandidate().getTutorMeetUserSN()));
            //e.getCandidate().setResume(null);
            e.getApplication().getCandidate().setFavoriteJobs(null);
            e.getApplication().getCandidate().setPassword(null);

        }
        return reports;
    }

    @Override
    public List<Candidate> getInterviewedCandidates(long userID, Timestamp start, Timestamp end) {
        ArrayList<Candidate> candidates = this.companyRepository.getInterviewedCandidates(userID, start, end);

        if (candidates == null || candidates.isEmpty()) return Collections.emptyList();

        return candidates;
    }
}
