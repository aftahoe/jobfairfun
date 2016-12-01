package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Job;
import org.json.JSONObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

public interface CompanyService {
    ////////////////// Company Level //////////////////////////////
    Company addCompany(Company company);

    long addCompany(Company company, MultipartFile multipartFile, String iconURL) throws IOException;

    Collection<Company> getCompanyByName(String name) throws UnsupportedEncodingException;

    Company getCompany(long id);


    /////////////////  Job Level ////////////////////////////////
    Job getJob(long joblongid);

    Job getJob(String uuid);

    Page getCompanyJobs(String companyUUID, Pageable pageable);

    Collection<Job> searchRecommendedJobs(long candidateId);

    Collection<Job> searchJobs(String keyword, String location, int salary, String sort);

    ArrayList<EvaluationQuestion> getEvaluationQuestions(long companyID);

    long putEvaluationResult(EvaluationReport report);

    JSONObject generateWaitingJobJSON(Candidate candidate, Job job);

    JSONObject generateJobJSONObjectForCandidate(Job job);
}
