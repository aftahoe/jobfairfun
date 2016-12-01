package job.fair.jobfair.jpa.repository;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Job;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;


public interface CompanyRepository {

    /**
     * Set the UUID of the Company if it is null and then save into the DB
     *
     * @param company company to be saved
     * @return saved comapany
     */
    Company addCompany(Company company);

    /**
     * Get company in DB by its id
     *
     * @param id of the company
     * @return <code>null</code> if the company does not exist. Company object if the company exists.
     */
    Company getCompany(long id);

    /**
     * Get company in DB by its email domain
     *
     * @param emailDomain of the company
     * @return <code>null</code> if the company does not exist. Company object if the company exists.
     */
    Company getCompanyByDomain(String emailDomain);

    /**
     * Get company list in DB by its name
     *
     * @param name of the company
     * @return collection of companies with that name
     */
    Collection<Company> getCompanyByName(String name) throws UnsupportedEncodingException;

    /**
     * Eager fetch all companies in DB contains theirs jobs
     *
     * @return collection of companies with jobs
     */
    Collection<Company> getAllCompanies();

    /**
     * Get the company with the specific id and update its icon link in DB
     *
     * @param companyId
     * @param iconLink
     */
    void updateIconLink(long companyId, String iconLink);

    /**
     * Get all jobs in the jobfair
     *
     * @return a list of all jobs.
     */
    Collection<Job> getAllJobs();

    /**
     * Get all jobs under the specific company
     *
     * @param companyUUID
     * @return a list of jobs under the company with UUID companyUUID
     */
    Page<Job> getCompanyJobs(String companyUUID, Pageable pageable);

    /**
     * Get all jobs under the specific company
     *
     * @param companyID
     * @return a list of jobs under the company with id companyID
     */
    Page<Job> getCompanyJobsBasicInfo(long companyID, Pageable pageable);


    /**
     * Get the job object by id
     *
     * @param id
     * @return job object
     */
    Job getJob(long id);

    /**
     * Get the job object by uuid
     *
     * @param uuid
     * @return job object
     */
    Job getJob(String uuid);

    /**
     * get company interview evaluation questions
     *
     * @param companyID
     * @return a list of evaluation questions
     */
    ArrayList<EvaluationQuestion> getEvaluationQuestions(long companyID);

    /**
     * put a new interview evaluation report
     *
     * @param report
     * @return report id
     */
    long putEvaluationReport(EvaluationReport report);

    /**
     * Get evaluation report according to the specific interviewer
     *
     * @param interviewerID
     * @return a list of evaluation reports
     */
    Page<EvaluationReport> getEvaluationReport(long interviewerID, String jobUUID, Pageable pageable);

    /**
     * Get evaluation report according to the specific interviewer
     *
     * @param interviewerID
     * @return a list of evaluation reports
     */
    Page<EvaluationReport> getEvaluationReport(long interviewerID, Pageable pageable);

    /**
     * Get all jobs that the candidate interviewed with during the time period
     *
     * @param candidateID
     * @param start
     * @param end
     * @return a list of jobs that the candidate interviewed with
     */
    ArrayList<Job> getCandidateInterviewedJobs(long candidateID, Timestamp start, Timestamp end);

    /**
     * Get all jobs that the candidate interviewed.
     *
     * @param candidateID
     * @return a list of jobs that the candidate interviewed with
     */
    ArrayList<Job> getCandidateInterviewedJobs(long candidateID);

    /**
     * Get all jobs that the interviewer interviewed with during the time period
     *
     * @param interviewerID
     * @param start
     * @param end
     * @return a list of jobs that the candidate interviewed with
     */
    ArrayList<Job> getInterviewerInterviewedJobs(long interviewerID, Timestamp start, Timestamp end);

    /**
     * Get the count of recommended candidates by the interviewer
     *
     * @param jobId
     * @param interviewerId
     * @return number of recommended candidates
     */
    int getRecommendationCount(long jobId, long interviewerId);

    /**
     * Get the candidates that interviewed with the interviewer
     *
     * @param interviewerID
     * @param start
     * @param end
     * @return a list of candidates
     */
    ArrayList<Candidate> getInterviewedCandidates(long interviewerID, Timestamp start, Timestamp end);

    //for test

    /**
     * Update the evaluation timestamp for testing
     *
     * @param id
     */
    void updateEvaluationReportTimestamp(long id);
}
