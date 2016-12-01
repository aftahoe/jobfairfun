package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by annawang on 3/2/16.
 */
public interface InterviewerService {

    Page<Job> getInterviewerJobs(long jobfairUserId, long interviewerId, Pageable pageable);

    Interviewer getInterviewer(long interviewerId);

    Interviewer getInterviewerByEmail(String email);

    Interviewer updateOrGetInterviewerMeetingID(long interviewerID) throws IOException, URISyntaxException;

    Page<EvaluationReport> getInterviewerInterviewJobs(long interviewerID, String jobId, Pageable pageable);

    List<Candidate> getInterviewedCandidates(long userID, Timestamp start, Timestamp end);
}
