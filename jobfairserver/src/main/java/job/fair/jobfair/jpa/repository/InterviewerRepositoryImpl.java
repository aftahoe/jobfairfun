package job.fair.jobfair.jpa.repository;

import job.fair.commons.IdGenerator;
import job.fair.jobfair.jpa.entity.Interviewer;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by annawang on 2/23/16.
 */
@Transactional
@Repository
public class InterviewerRepositoryImpl implements InterviewerRepository {
    static final Logger logger = Logger.getLogger(CandidateRepositoryImpl.class);
    // Injected database connection:
    @PersistenceContext
    private EntityManager em;


    /**
     * {@inheritDoc}
     */
    @Override
    public Interviewer addInterviewer(Interviewer interviewer) {
        interviewer.setUuid(IdGenerator.createId());
        em.persist(interviewer);
        return interviewer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Interviewer updateInterviewer(String email, Interviewer interviewer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // The input interviewer must have email
        Interviewer currentInterviewer = this.getInterviewerByEmail(email);

        Field[] fields = interviewer.getClass().getDeclaredFields();
        for (Field field : fields) {
            Class type = field.getType();
            String fieldName = field.getName();
            field.setAccessible(true);
            Object fieldValue = field.get(interviewer);

            if (type == boolean.class) {
                //TODO
            } else if (fieldName.equals("id") || fieldName.equals("uuid") || fieldName.equals("email")) {
                //TODO   do nothing
            } else if (type.isPrimitive() && ((Number) fieldValue).intValue() != 0) {
                fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String methodName = "set" + fieldName;
                Method m = interviewer.getClass().getMethod(methodName, type);
                m.invoke(currentInterviewer, fieldValue);
            }
            // found default value
            else if (!type.isPrimitive() && fieldValue != null) {
                fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                String methodName = "set" + fieldName;
                Method m = interviewer.getClass().getMethod(methodName, type);
                m.invoke(currentInterviewer, fieldValue);
            }
        }

        return currentInterviewer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistingInterviewer(String email) {
        Query query = this.em.createQuery("SELECT COUNT(c.email) FROM Interviewer c WHERE c.email=:emailparam");
        query.setParameter("emailparam", email);
        return (long) query.getSingleResult() != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Interviewer getInterviewerByEmail(String email) {
        Query query = this.em.createQuery("SELECT c FROM Interviewer c WHERE c.email=:emailparam");
        query.setParameter("emailparam", email);
        if (query.getResultList().size() == 0) return null;
        else return (Interviewer) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Interviewer getInterviewer(long id) {
        Interviewer c = em.find(Interviewer.class, id);
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Interviewer getInterviewer(String uuid) {
        Query query = this.em.createQuery("SELECT c FROM Interviewer c WHERE c.uuid=:uuid");
        query.setParameter("uuid", uuid);
        if (query.getResultList().size() == 0) return null;
        else return (Interviewer) query.getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInterviewerMeetingID(long id, String meetingId) {
        Interviewer in = em.find(Interviewer.class, id);
        in.setMeetingID(meetingId);
    }
}
