package job.fair.jobfair.scheduler;

import job.fair.jobfair.jpa.entity.Candidate;

import java.util.LinkedList;

/**
 * Interface for scheduler
 */
public interface Scheduler extends Runnable {
    String getName();

    LinkedList<Candidate> getJobsWaiting();

    LinkedList<Candidate> getJobsRunning();

    int submit(Candidate job);

    Candidate removeFromWaitingQueue(long candidateId, String jobuuid, String companyName, String positionName);

    int searchPosition(long candidateId);

    void stop();
}
