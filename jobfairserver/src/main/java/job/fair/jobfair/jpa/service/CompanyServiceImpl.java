package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.InterviewerRepository;
import job.fair.jobfair.scheduler.Scheduler;
import job.fair.jobfair.scheduler.SchedulerFactory;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CandidateRepository candidateRepository;

    @Autowired
    InterviewerRepository interviewerRepository;


    @Override
    public Company addCompany(Company company) {
        return companyRepository.addCompany(company);
    }

    @Override
    public long addCompany(Company company, MultipartFile file, String iconURL) throws IOException {
        long id = companyRepository.addCompany(company).getId();
        File dir = new File("/tmp/icons");  //TODO change the directory
        if (!dir.exists()) {
            boolean result = false;
            try {
                dir.mkdir();
                result = true;
            } catch (SecurityException se) {
                //handle it TODO
            }
            if (result) {
                System.out.println("DIR created"); //TODO
            }
        }

        String iconFileName = id + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        // 1. Save the icon
        String fileName = dir + File.separator + iconFileName;
        file.transferTo(new File(fileName));
        // 2. update company icon link
        companyRepository.updateIconLink(id, iconURL + iconFileName);
        return id;
    }

    @Override
    public Collection<Company> getCompanyByName(String name) throws UnsupportedEncodingException {
        return companyRepository.getCompanyByName(name);
    }

    @Override
    public Collection<Job> searchJobs(String keyword, String location, int salary, String sort) {
        Collection<Company> companies = this.companyRepository.getAllCompanies();
        StringBuilder text = new StringBuilder();

        List<Job> jobs = new ArrayList<>();
        for (Company c : companies) {
            text.append(c.getEmailDomain()).append(" ");
            text.append(c.getName());
            for (Job j : c.getJobs()) {
                text.append(j.getContractType()).append(" ");
                text.append(j.getDescription()).append(" ");
                text.append(j.getLocation()).append(" ");
                text.append(j.getPositionName()).append(" ");
                text.append(j.getPositionNumber()).append(" ");
                for (String s : j.getRequiredSkills()) {
                    text.append(s).append(" ");
                }
                String allText = formatBeforeComparing(text.toString());
                if (allText.contains(formatBeforeComparing(keyword))) {
                    jobs.add(j);
                }

                text.setLength(0);
                text.append(c.getEmailDomain()).append(" ");
                text.append(c.getName());
            }
        }

        if (location != null) {
            jobs.removeIf(job -> !formatBeforeComparing(job.getLocation()).contains(formatBeforeComparing(location)));
        }
        if (salary != 0) {
            jobs.removeIf(job -> job.getCompensation() < salary);
        }
        if (sort == null || sort.isEmpty() || jobs == null || jobs.isEmpty()) return jobs;
        return sorting(jobs, sort);
    }

    private List<Job> sorting(List<Job> jobs, String sortBy) {
        if (sortBy.equals(Job.SORTING_ASC_COMPANY_NAME)) {
            jobs.sort((Job j1, Job j2) -> j1.getCompanyName().compareTo(j2.getCompanyName()));
        } else if (sortBy.equals(Job.SORTING_DESC_COMPANY_NAME)) {
            jobs.sort((Job j1, Job j2) -> (-1) * j1.getCompanyName().compareTo(j2.getCompanyName()));
        } else if (sortBy.equals(Job.SORTING_ASC_POSITION_NAME)) {
            jobs.sort((Job j1, Job j2) -> j1.getPositionName().compareTo(j2.getPositionName()));
        } else if (sortBy.equals(Job.SORTING_DESC_POSITION_NAME)) {
            jobs.sort((Job j1, Job j2) -> (-1) * j1.getPositionName().compareTo(j2.getPositionName()));
        } else if (sortBy.equals(Job.SORTING_ASC_LOCATION)) {
            jobs.sort((Job j1, Job j2) -> j1.getLocation().compareTo(j2.getLocation()));
        } else if (sortBy.equals(Job.SORTING_DESC_LOCATION)) {
            jobs.sort((Job j1, Job j2) -> (-1) * j1.getLocation().compareTo(j2.getLocation()));
        } else {
            return jobs;
        }
        return jobs;
    }

    @Override
    public Collection<Job> searchRecommendedJobs(long candidateId) {
        ArrayList<Job> jobs = this.companyRepository.getCandidateInterviewedJobs(candidateId);
        if (jobs == null || jobs.isEmpty()) return Collections.emptyList();

        Set<Job> recommendedJobs = new HashSet<>();
        for (Job job : jobs) {
            recommendedJobs.addAll(searchJobs(job.getPositionName(), null, 0, null));
        }

        List<Long> jobsId = jobs.parallelStream().map(Job::getId).collect(Collectors.toList());
        recommendedJobs.removeIf(job -> jobsId.contains(job.getId()));


        return recommendedJobs;
    }

    private String formatBeforeComparing(String string) {
        return string.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]+", " ");
    }

    @Override
    public Company getCompany(long id) {
        return companyRepository.getCompany(id);
    }

    @Override
    public Page getCompanyJobs(String companyUUID, Pageable pageable) {
        return companyRepository.getCompanyJobs(companyUUID, pageable);
    }

    @Override
    public Job getJob(long joblongid) {
        return companyRepository.getJob(joblongid);
    }


    @Override
    public ArrayList<EvaluationQuestion> getEvaluationQuestions(long companyID) {
        return companyRepository.getEvaluationQuestions(companyID);
    }

    @Override
    public long putEvaluationResult(EvaluationReport report) {

        return companyRepository.putEvaluationReport(report);
    }

    @Override
    public JSONObject generateWaitingJobJSON(Candidate candidate, Job job) {
        Scheduler scheduler = SchedulerFactory.singleton().createOrGetFIFOScheduler(candidate.getJobqueue());
        JSONObject returnObject = new JSONObject();
        returnObject.put("id", job.getUuid());
        returnObject.put("queuePosition", scheduler.searchPosition(candidate.getId()));
        returnObject.put("positionName", job.getPositionName());
        returnObject.put("companyName", job.getCompany().getName());

        return returnObject;
    }

    @Override
    public JSONObject generateJobJSONObjectForCandidate(Job job) {
        JSONObject basicJobInfo = new JSONObject();
        basicJobInfo.put("id", job.getUuid());
        basicJobInfo.put("positionNumber", job.getPositionNumber());
        basicJobInfo.put("positionName", job.getPositionName());
        basicJobInfo.put("location", job.getLocation());
        basicJobInfo.put("compensation", job.getCompensation());
        basicJobInfo.put("description", job.getDescription());
        basicJobInfo.put("companyName", job.getCompany().getName());

        return basicJobInfo;
    }

    @Override
    public Job getJob(String uuid) {
        return companyRepository.getJob(uuid);
    }
}
