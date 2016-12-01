package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.JobfairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Created by annawang on 2/5/16.
 */
@Service
public class JobfairServiceImpl implements JobfairService {
    @Autowired
    JobfairRepository jobfairRepository;

    @Autowired
    CompanyRepository companyRepository;


    @Override
    public long addJobfair(Jobfair jobfair) {
        return jobfairRepository.addJobfair(jobfair);
    }

    @Override
    public Jobfair getJobfair(long id) {
        return jobfairRepository.getJobfair(id);
    }

    @Override
    public void addCompanyToJobFair(long JobfairId, Company company) {
        jobfairRepository.addCompanyToJobfair(JobfairId, company);
    }

    @Override
    public Page<Company> getJobfairCompanies(long jobfairlongid, Pageable pageable) {
        return jobfairRepository.getJobfairCompaniesBasicInfo(jobfairlongid, pageable);
    }

    @Override
    public Collection<Jobfair> getAllJobfairs() {
        return jobfairRepository.getAllJobfairs();
    }

    @Override
    public long getJobFairId(String uuid) {
        return jobfairRepository.getJobfairId(uuid);
    }
}
