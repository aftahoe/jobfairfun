package job.fair.jobfair.jpa.repository;

import job.fair.jobfair.jpa.entity.Interviewer;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by annawang on 2/23/16.
 */
public interface InterviewerRepository {
    /**
     * Use interviewer email to check does the interviewer already exist
     *
     * @param email of the interviewer
     * @return true if the interviewer exists, false if not exists
     */
    boolean isExistingInterviewer(String email);


    /**
     * Get interviewer in DB by his/her email
     *
     * @param email of the interviewer
     * @return <code>null</code> if the interviewer does not exist. Interviewer object if the interviewer exists.
     */
    Interviewer getInterviewerByEmail(String email);

    /**
     * Get interviewer in DB by his/her id
     *
     * @param id of the interviewer
     * @return <code>null</code> if the interviewer does not exist. Interviewer object if the interviewer exists.
     */
    Interviewer getInterviewer(long id);

    /**
     * Get interviewer in DB by his/her uuid
     *
     * @param uuid of the interviewer
     * @return <code>null</code> if the interviewer does not exist. Interviewer object if the interviewer exists.
     */
    Interviewer getInterviewer(String uuid);

    /**
     * Set the UUID of the interviewer and then save into the DB
     *
     * @param interviewer interviewer to be saved
     * @return saved interviewer
     */
    Interviewer addInterviewer(Interviewer interviewer);

    /**
     * Get current interviewer by email and updated the fields by the new interviewer
     *
     * @param email       to get current interviewer
     * @param interviewer the new interviewer with fields that needs to be updated
     * @return updated interviewer
     * @throws IllegalAccessException    Reflection update error
     * @throws NoSuchMethodException     Reflection update error
     * @throws InvocationTargetException Reflection update error
     */
    Interviewer updateInterviewer(String email, Interviewer interviewer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    /**
     * Get the interviewer by id and update his/her meetingId in DB
     *
     * @param id        of the interviewer
     * @param meetingId of the interviewer
     */
    void updateInterviewerMeetingID(long id, String meetingId);
}
