package job.fair.jobfair.jpa.repository;

import job.fair.commons.IdGenerator;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.Resume;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by annawang on 1/26/16.
 */
@Transactional
@Repository
public class CandidateRepositoryImpl implements CandidateRepository {
    static final Logger logger = Logger.getLogger(CandidateRepositoryImpl.class);

    // Injected database connection:
    @PersistenceContext
    private EntityManager em;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistingCandidate(String email) {
        Query query = this.em.createQuery("SELECT COUNT(c.email) FROM Candidate c WHERE c.email=:emailparam");
        query.setParameter("emailparam", email);
        return (long) query.getSingleResult() != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Candidate addCandidate(Candidate candidate) {
        if (candidate.getUuid() == null || candidate.getUuid().isEmpty())
            candidate.setUuid(IdGenerator.createId());
        em.persist(candidate);
        return candidate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resume addResume(Resume resume) {
        if (resume.getUuid() == null || resume.getUuid().isEmpty())
            resume.setUuid(IdGenerator.createId());
        em.persist(resume);
        return resume;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Candidate getCandidate(long id) {
        Candidate candidate = em.find(Candidate.class, id);
        return candidate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Candidate getCandidateByEmail(String email) {
        Query query = this.em.createQuery("SELECT c FROM Candidate c WHERE c.email=:emailparam");
        query.setParameter("emailparam", email);
        if (query.getResultList().size() == 0) return null;
        else return (Candidate) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Candidate getCandidate(String uuid) {
        Query query = this.em.createQuery("SELECT c FROM Candidate c WHERE c.uuid=:uuid");
        query.setParameter("uuid", uuid);
        if (query.getResultList().size() == 0) return null;
        else return (Candidate) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override //TODO remove checkDB laster
    public Candidate updateCandidate(String email, Candidate candidate, boolean checkDB) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // The input interviewer must have email
        Candidate currentCandidate = this.getCandidateByEmail(email);

        if (checkDB && currentCandidate.getJobqueue() != null && !currentCandidate.getJobqueue().isEmpty()) {
            Candidate ca = new Candidate();
            ca.setJobqueue(currentCandidate.getJobqueue());
            return ca;
        }
        updateCandidateWithReflection(candidate, currentCandidate);

        return currentCandidate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Candidate updateCandidateStatus(long candidateId, Candidate.CandidateStatus status) {
        Candidate candidate = em.find(Candidate.class, candidateId);
        candidate.setStatus(status);
        return candidate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCandidateJobQueue(long userid) {
        Candidate candidate = em.find(Candidate.class, userid);
        candidate.setJobqueue("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Job> addFavoriteJob(long candidateID, Job job) {
        Candidate candidate = this.em.find(Candidate.class, candidateID);
        candidate.addFavoriteJob(job);

        return candidate.getFavoriteJobs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Job> getFavoriteJob(long candidateID) {
        Query query = this.em.createQuery("SELECT DISTINCT c.favoriteJobs FROM Candidate c WHERE c.id =:id");
        query.setParameter("id", candidateID);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Job> deleteFavoriteJob(long candidateID, String jobuuid) {
        Candidate candidate = this.em.find(Candidate.class, candidateID);
        candidate.removeFavoriteJob(jobuuid);

        return candidate.getFavoriteJobs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Candidate.CandidateStatus getCandidateStatus(long id) {
        Query query = this.em.createQuery("SELECT DISTINCT c.status FROM Candidate c WHERE c.id =:id");
        query.setParameter("id", id);
        if (query.getResultList().size() == 0) return null;
        return (Candidate.CandidateStatus) query.getSingleResult();
    }

    @Override
    public void addApplication(Application application) {
        application.setUuid(IdGenerator.createId());
        em.persist(application);
    }

    @Override
    public ArrayList<Application> getApplications(long candidateID, long jobID) {
        Query query = this.em.createQuery("SELECT DISTINCT a FROM Application a " +
                "WHERE a.candidate.id =:candidateid AND a.job.id=:jobid ORDER BY a.timestamp DESC");
        query.setParameter("candidateid", candidateID);
        query.setParameter("jobid", jobID);
        if (query.getResultList().size() == 0) return new ArrayList<>();
        return (ArrayList<Application>) query.getResultList();
    }

    @Override
    public ArrayList<Application> getApplications(long candidateID) {
        Query query = this.em.createQuery("SELECT DISTINCT a FROM Application a " +
                "WHERE a.candidate.id =:candidateid ORDER BY a.timestamp DESC");
        query.setParameter("candidateid", candidateID);
        if (query.getResultList().size() == 0) return new ArrayList<>();
        return (ArrayList<Application>) query.getResultList();
    }

    private void updateCandidateWithReflection(Candidate candidate, Candidate currentCandidate) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Field[] fields = candidate.getClass().getDeclaredFields();
        for (Field field : fields) {
            Class type = field.getType();
            String fieldName = field.getName();
            field.setAccessible(true);
            Object fieldValue = field.get(candidate);

            if (type == boolean.class) {
                //TODO
            } else if (fieldName.equals("id") || fieldName.equals("uuid") || fieldName.equals("email")) {
                //TODO   do nothing
            } else if (type.isPrimitive() && ((Number) fieldValue).intValue() != 0 && !fieldName.equals("serialVersionUID")) {
                fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String methodName = "set" + fieldName;
                Method m = candidate.getClass().getMethod(methodName, type);
                m.invoke(currentCandidate, fieldValue);
            }
            // found default value
            else if (!type.isPrimitive() && fieldValue != null) {
                fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String methodName = "set" + fieldName;
                Method m = candidate.getClass().getMethod(methodName, type);
                m.invoke(currentCandidate, fieldValue);
            }
        }
    }
}
