package job.fair.jobfair.jpa.repository;

import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * Created by annawang on 2/5/16.
 */
public interface JobfairRepository {
    long addJobfair(Jobfair jobfair);

    Jobfair getJobfair(long id);

    void addCompanyToJobfair(long id, Company company);

    Page<Company> getJobfairCompaniesBasicInfo(long jobfairlongid, Pageable pageable);

    Collection<Jobfair> getAllJobfairs();

    long getJobfairId(String uuid);
}
