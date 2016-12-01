package job.fair.jobfair.jpa.repository;


import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Resume;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by annawang on 1/26/16.
 */
public interface CandidateRepository {
    /**
     * Use candidate email to check does the candidate already exist
     *
     * @param email of the candidate
     * @return true if the candidate exists, false if not exists
     */
    boolean isExistingCandidate(String email);

    /**
     * Set the UUID of the candidate and then save into the DB
     *
     * @param candidate candidate to be saved
     * @return saved candidate
     */
    Candidate addCandidate(Candidate candidate);

    /**
     * Get candidate in DB by his/her id
     *
     * @param candidateID of the candidate
     * @return <code>null</code> if the candidate does not exist. Candidate object if the candidate exists.
     */
    Candidate getCandidate(long candidateID);

    /**
     * Get candidate in DB by his/her uuid
     *
     * @param candidateUUID of the candidate
     * @return <code>null</code> if the candidate does not exist. Candidate object if the candidate exists.
     */
    Candidate getCandidate(String candidateUUID);

    /**
     * Get candidate in DB by his/her email
     *
     * @param email of the candidate
     * @return <code>null</code> if the candidate does not exist. Candidate object if the candidate exists.
     */
    Candidate getCandidateByEmail(String email);

    /**
     * Get current candidate by email and updated the fields by the new candidate
     *
     * @param email     to get current candidate
     * @param candidate the new candidate with fields that needs to be updated
     * @param checkDB   this is a temporary check to see if we care about use alreayd applied job or not
     * @return updated candidate
     * @throws IllegalAccessException    Reflection update error
     * @throws NoSuchMethodException     Reflection update error
     * @throws InvocationTargetException Reflection update error
     */
    Candidate updateCandidate(String email, Candidate candidate, boolean checkDB) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    /**
     * Set jobqueue field to empty while candidate left the queue
     *
     * @param candidateID
     */
    void removeCandidateJobQueue(long candidateID);

    /**
     * Add one more candidate's favorite job
     *
     * @param candidateID
     * @param job
     * @return a list of all candidate's favorite jobs
     */
    Collection<Job> addFavoriteJob(long candidateID, Job job);

    void addApplication(Application application);

    ArrayList<Application> getApplications(long candidateID, long jobID);

    ArrayList<Application> getApplications(long candidateID);

    /**
     * Get all candidate's favorite jobs
     *
     * @param candidateID
     * @return a list of all candidate's favorite jobs
     */
    Collection<Job> getFavoriteJob(long candidateID);

    /**
     * delete one of candidate's favorite jobs
     *
     * @param candidateID
     * @param jobuuid
     * @return a list of all candidate's remaining favorite jobs
     */
    Collection<Job> deleteFavoriteJob(long candidateID, String jobuuid);

    /**
     * Get current candidate's status
     *
     * @param id
     * @return candidate's status
     */
    Candidate.CandidateStatus getCandidateStatus(long id);

    /**
     * Update candidate's status
     *
     * @param candidateId
     * @param status
     * @return the candidate object
     */
    Candidate updateCandidateStatus(long candidateId, Candidate.CandidateStatus status);

    Resume addResume(Resume resume);
}
