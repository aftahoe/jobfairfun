package job.fair.jobfair.jpa.service;

import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Resume;
import job.fair.jobfair.jpa.entity.User;
import org.json.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

/**
 * Created by annawang on 1/26/16.
 */
public interface CandidateService {

    void signup(Candidate candidate) throws IOException, URISyntaxException, CandidateException;

    boolean isExistingCandidate(Candidate candidate);

    long addCandidate(Candidate candidate);

    Candidate getCandidateByEmail(String email);

    Candidate getCandidate(String uuid);

    Candidate getCandidate(long id);

    Application applyJob(Job job, User user, Application application, List<MultipartFile> resumes) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Application getApplication(long candidateId, long jobId);

    Application getLatestApplication(long jobfairUserId);

    Candidate wipeOutForCandidate(Candidate c);

    Candidate updateCandidateProfile(String candidateEmail, Candidate candidate) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    float calculateJobTestScore(Job job, JSONArray answers);

    Candidate updateCandidateStatus(Candidate candidate);

    List<Candidate> removeUserFromQueue(Job job, long userid, String userUUID, User.Type type);

    Candidate.CandidateStatus getCandidateStatus(long id);

    Collection<Job> putFavoriteJob(long candidateID, Job job);

    Collection<Job> getFavoriteJobs(long candidateID);

    Collection<Job> deleteFavoriteJob(long candidateID, String jobUUID);

    Resume addResume(Resume resume);
}
