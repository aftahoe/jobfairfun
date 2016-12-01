package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * Created by annawang on 2/5/16.
 */
public interface JobfairService {
    long addJobfair(Jobfair jobfair);

    Jobfair getJobfair(long id);

    void addCompanyToJobFair(long id, Company company);

    Page<Company> getJobfairCompanies(long jobfairlongid, Pageable pageable);

    Collection<Jobfair> getAllJobfairs();

    long getJobFairId(String uuid);
}
