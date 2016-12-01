package job.fair.jobfair.jpa.repository;

import job.fair.commons.IdGenerator;
import job.fair.data.Page;
import job.fair.jobfair.Exception.JobfairException;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.Jobfair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by annawang on 2/5/16.
 */
@Transactional
@Repository
public class JobfairRepositoryImpl implements JobfairRepository {

    // Injected database connection:
    @PersistenceContext
    private EntityManager em;

    @Override
    public long addJobfair(Jobfair jobfair) {
        if (jobfair.getUuid() == null || jobfair.getUuid().isEmpty()) {
            jobfair.setUuid(IdGenerator.createId());
        }
        em.persist(jobfair);
        return jobfair.getId();
    }

    @Override
    public Jobfair getJobfair(long id) {
        Jobfair jobfair = em.find(Jobfair.class, id);
        return jobfair;
    }

    @Override
    public void addCompanyToJobfair(long id, Company company) {
        Jobfair jobfair = em.find(Jobfair.class, id);

        Company c = em.find(Company.class, company.getId());
        if (c == null)
            jobfair.addCompany(company);
        else {
            if (c.getJobfair() != null) {
                throw new JobfairException("Company " + company.getId() +
                        " already belong to jobfair " + c.getJobfair().getId());
            }
            c.setJobfair(jobfair);
        }

    }

    @Cacheable(value = "jobfaircompanies")
    @Override
    public Page<Company> getJobfairCompaniesBasicInfo(long jobfairlongid, Pageable pageable) {
        Query query = null;
        if (jobfairlongid == 0) {
            query = em.createQuery("SELECT c FROM Company c");
        } else {
            query = em.createQuery("SELECT companies FROM Jobfair j " +
                    "WHERE j.id=" + jobfairlongid);
        }

        if (query.getResultList().size() == 0) return new Page<>(Collections.EMPTY_LIST, 0);

        int size = query.getResultList().size();

        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        List<Company> companies = query.getResultList();

        // Since we use lazy fetch, we will set these to null //TODO other way?
        for (Company c : companies) {
            Query countQuery = em.createQuery("SELECT COUNT(j.id) FROM Job j where j.company.id=" + c.getId());
            c.setCount((long) countQuery.getSingleResult());
            c.setJobs(null);
        }

        return new Page<>(companies, size);
    }

    @Override
    public Collection<Jobfair> getAllJobfairs() {
        Query query = em.createQuery("SELECT j FROM Jobfair j order by j.startDate");
        Collection<Jobfair> jobfairs = query.getResultList();
        // Since we use lazy fetch, we will set these to null //TODO other way?
        for (Jobfair j : jobfairs) {
            Query countQuery = em.createQuery("SELECT COUNT(c.id) FROM Company c where c.jobfair.id=" + j.getId());
            j.setCompanies(null);
            j.setCompanyCount((long) countQuery.getSingleResult());
        }
        return jobfairs;
    }

    @Override
    public long getJobfairId(String uuid) {
        Query query = this.em.createQuery("SELECT DISTINCT j.id FROM Jobfair j WHERE j.uuid =:uuid");
        query.setParameter("uuid", uuid);
        if (query.getResultList().size() == 0) return 0;
        return ((Long) query.getSingleResult()).longValue();
    }
}
