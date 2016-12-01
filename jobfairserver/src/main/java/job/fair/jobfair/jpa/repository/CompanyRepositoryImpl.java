package job.fair.jobfair.jpa.repository;

import job.fair.commons.IdGenerator;
import job.fair.data.Page;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Job;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Transactional
@Repository
public class CompanyRepositoryImpl implements CompanyRepository {
    static final Logger logger = Logger.getLogger(CompanyRepositoryImpl.class);
    // Injected database connection:
    @PersistenceContext
    private EntityManager em;

    /**
     * {@inheritDoc}
     */
    @Override
    public Company addCompany(Company company) {
        if (company.getUuid() == null || company.getUuid().isEmpty())
            company.setUuid(IdGenerator.createId());

        em.persist(company);
        return company;
    }

    /**
     * {@inheritDoc}
     */
    // eager fetch all companies with job info
    @Override
    public Collection<Company> getCompanyByName(String name) throws UnsupportedEncodingException {
        Query query = this.em.createQuery("SELECT DISTINCT c FROM Company c left join FETCH c.jobs " +
                "WHERE c.name =:name");
        query.setParameter("name", name);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company getCompany(long id) {
        return em.find(Company.class, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job getJob(long id) {
        return em.find(Job.class, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job getJob(String uuid) {
        Query query = this.em.createQuery("SELECT DISTINCT j FROM Job j " +
                "WHERE j.uuid =:uuid");

        query.setParameter("uuid", uuid);
        if (query.getResultList().size() == 0) return null;
        return (Job) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "companyjobs")
    @Override
    public Page getCompanyJobs(String companyUUID, Pageable pageable) {
        Query query = this.em.createQuery("SELECT j from Job j where j.company.uuid =:uuid");
        query.setParameter("uuid", companyUUID);
        if (query.getResultList().size() == 0) return null;

        int totalSize = query.getResultList().size();
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return new Page(query.getResultList(), totalSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company getCompanyByDomain(String emailDomain) {
        Query query = this.em.createQuery("SELECT DISTINCT c FROM Company c " +
                "WHERE c.emailDomain =:emailDomain");
        query.setParameter("emailDomain", emailDomain);
        if (query.getResultList().size() == 0) return null;
        return (Company) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Job> getCompanyJobsBasicInfo(long companylongID, Pageable pageable) {
        Query query = this.em.createQuery("SELECT DISTINCT " +
                "new Job(j.id, j.uuid, j.positionNumber, j.positionName, j.positionCount) " +
                "FROM Job j WHERE j.company.id =:id");
        query.setParameter("id", companylongID);
        if (query.getResultList().size() == 0) {
            return new Page<>(Collections.EMPTY_LIST, 0);
        }

        int totalSize = query.getResultList().size();
        logger.debug("this is total number " + totalSize);
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return new Page<>(query.getResultList(), totalSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<EvaluationQuestion> getEvaluationQuestions(long companyID) {
        Query query = this.em.createQuery("SELECT DISTINCT c.evaluationQuestions FROM Company c WHERE c.id =:id");
        query.setParameter("id", companyID);
        if (query.getResultList().size() == 0) return new ArrayList<EvaluationQuestion>();
        return (ArrayList<EvaluationQuestion>) query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long putEvaluationReport(EvaluationReport report) {
        this.em.persist(report);
        return report.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "companies")
    @Override
    public Collection<Company> getAllCompanies() {
        Query query = em.createQuery("SELECT DISTINCT c FROM Company c left join FETCH c.jobs");
        Collection<Company> companies = (Collection<Company>) query.getResultList();

        return companies;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = "jobs")
    @Override
    public Collection<Job> getAllJobs() {
        Query query = em.createQuery("SELECT DISTINCT j FROM Job j");
        Collection<Job> jobs = (Collection<Job>) query.getResultList();
        return jobs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateIconLink(long id, String iconURL) {
        Company c = em.find(Company.class, id);
        c.setIcon(iconURL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEvaluationReportTimestamp(long id) {
        EvaluationReport report = this.em.find(EvaluationReport.class, id);
        Date current = report.getTimestamp();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.DAY_OF_WEEK, -1);
        Date yesterday = new Date(cal.getTime().getTime());
        report.setTimestamp(new Timestamp(yesterday.getTime()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EvaluationReport> getEvaluationReport(long interviewerID, String jobUUID, Pageable pageable) {
        logger.info("get the evaluation report with interviewerID: " + interviewerID);
        Query query = this.em.createQuery("SELECT DISTINCT e FROM EvaluationReport e " +
                "WHERE e.interviewer.id =:interviewerid AND e.recommendationType =:type AND e.application.job.uuid=:jobuuid " +
                "ORDER BY e.timestamp DESC");
        query.setParameter("interviewerid", interviewerID);
        query.setParameter("jobuuid", jobUUID);
        query.setParameter("type", EvaluationReport.Type.RECOMMENDED);
        if (query.getResultList().size() == 0) new Page<>(Collections.EMPTY_LIST, 0);

        int totalSize = query.getResultList().size();
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return new Page<>(query.getResultList(), totalSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EvaluationReport> getEvaluationReport(long interviewerID, Pageable pageable) {
        logger.info("get the evaluation report with interviewerID: " + interviewerID);
        Query query = this.em.createQuery("SELECT DISTINCT e FROM EvaluationReport e " +
                "WHERE e.interviewer.id =:interviewerid AND e.recommendationType =:type " +
                "ORDER BY e.timestamp DESC");
        query.setParameter("interviewerid", interviewerID);
        query.setParameter("type", EvaluationReport.Type.RECOMMENDED);

        if (query.getResultList().size() == 0) new Page<>(Collections.EMPTY_LIST, 0);

        int totalSize = query.getResultList().size();
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return new Page<>(query.getResultList(), totalSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Job> getCandidateInterviewedJobs(long candidateID, Timestamp start, Timestamp end) {
        Date dateStart = new Date(start.getTime());
        Date dateEnd = new Date(end.getTime());
        String queryString = "SELECT DISTINCT application.job FROM EvaluationReport e " +
                "WHERE e.application.candidate.id =" + candidateID + " AND e.timestamp > " + dateStart.toString() +
                " AND e.timestamp < " + dateEnd.toString() +
                "ORDER BY e.timestamp DESC";
        logger.debug("Get candidate interviewed jobs with query: " + queryString);
        Query query = this.em.createQuery("SELECT DISTINCT application.job FROM EvaluationReport e " +
                "WHERE e.application.candidate.id =:candidateid AND e.timestamp > :start AND e.timestamp < :end " +
                "ORDER BY e.timestamp DESC");
        query.setParameter("candidateid", candidateID);
        query.setParameter("start", start);
        query.setParameter("end", end);
        if (query.getResultList().size() == 0) return new ArrayList<>();
        return (ArrayList<Job>) query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Job> getInterviewerInterviewedJobs(long interviewerID, Timestamp start, Timestamp end) {
        String queryString = "SELECT DISTINCT application.job FROM EvaluationReport e " +
                "WHERE e.interviewer.id =" + interviewerID + " AND e.timestamp > " + start.toString() +
                " AND e.timestamp < " + end.toString() +
                "ORDER BY e.timestamp DESC";
        logger.debug("Get interviewer interviewed jobs with query: " + queryString);
        Query query = this.em.createQuery("SELECT DISTINCT application.job FROM EvaluationReport e " +
                "WHERE e.interviewer.id =:interviewerid AND e.timestamp > :start AND e.timestamp < :end " +
                "ORDER BY e.timestamp DESC");
        query.setParameter("interviewerid", interviewerID);
        query.setParameter("start", start);
        query.setParameter("end", end);
        if (query.getResultList().size() == 0) return null;
        return (ArrayList<Job>) query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Job> getCandidateInterviewedJobs(long candidateID) {
        Query query = this.em.createQuery("SELECT DISTINCT application.job FROM EvaluationReport e " +
                "WHERE e.application.candidate.id =:candidateid");
        query.setParameter("candidateid", candidateID);
        if (query.getResultList().size() == 0) return null;
        return (ArrayList<Job>) query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Candidate> getInterviewedCandidates(long interviewerID, Timestamp start, Timestamp end) {
        String queryString = "SELECT application.candidate FROM EvaluationReport e " +
                "WHERE e.interviewer.id =" + interviewerID + " AND e.timestamp > " + start.toString() +
                " AND e.timestamp <  " + end.toString() +
                "ORDER BY e.timestamp DESC";
        logger.debug("Get interviewer interviewed candidates with query: " + queryString);
        Query query = this.em.createQuery("SELECT application.candidate FROM EvaluationReport e " +
                "WHERE e.interviewer.id =:interviewerid AND e.timestamp > :start AND e.timestamp < :end " +
                "ORDER BY e.timestamp DESC");
        query.setParameter("interviewerid", interviewerID);
        query.setParameter("start", start);
        query.setParameter("end", end);
        if (query.getResultList().size() == 0) return new ArrayList<>();
        return (ArrayList<Candidate>) query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRecommendationCount(long jobId, long interviewerId) {
        Query query = this.em.createQuery("SELECT DISTINCT e.id FROM EvaluationReport e " +
                "WHERE e.application.job.id =:jobid AND e.interviewer.id =:interviewerid AND e.recommendationType =:type");
        query.setParameter("jobid", jobId);
        query.setParameter("interviewerid", interviewerId);
        query.setParameter("type", EvaluationReport.Type.RECOMMENDED);
        return query.getResultList().size();
    }
}
