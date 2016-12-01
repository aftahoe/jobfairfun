package job.fair.jobfair.scheduler;

import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.wsclient.WSClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_ACTION;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_ACTION_CHANGEPOSITION;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITION;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME;

/**
 * Created by annawang on 2/17/16.
 */
public class FIFOScheduler implements Scheduler {
    static Logger LOGGER = LoggerFactory.getLogger(FIFOScheduler.class);
    List<Candidate> queue = new LinkedList<>();
    boolean terminate = false;
    Candidate runningJob = null;
    private ExecutorService executor;
    private SchedulerListener listener;
    private String name;

    public FIFOScheduler(String name, ExecutorService executor, SchedulerListener listener) {
        LOGGER.info("fifo constructor");
        this.name = name;
        this.executor = executor;
        this.listener = listener;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LinkedList<Candidate> getJobsWaiting() {
        System.out.println("@@@@@@@@@@waiting " + queue.size());
        LinkedList<Candidate> ret = new LinkedList<>();
        synchronized (queue) {
            for (Candidate job : queue) {
                ret.add(job);
            }
        }
        return ret;
    }

    @Override
    public int searchPosition(long candidateId) {
        LinkedList<Candidate> candidates = getJobsWaiting();
        synchronized (candidates) {
            int i = 1;
            for (Candidate c : candidates) {
                if (c.getId() == candidateId) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    @Override
    public LinkedList<Candidate> getJobsRunning() {
        LinkedList<Candidate> ret = new LinkedList<Candidate>();
        Candidate job = runningJob;

        if (job != null) {
            ret.add(job);
        }

        return ret;
    }


    @Override
    public int submit(Candidate candidate) {
        candidate.setQueueStatus(Candidate.Status.PENDING);
        candidate.setPassword("");
        candidate.setTutorMeetApitoken("");
        candidate.setTutorMeetToken("");
        synchronized (queue) {
            queue.add(candidate);
            queue.notify();
        }
        candidate.setJobqueue(this.getName());
        System.out.println("################## size " + queue.size());
        return getJobsWaiting().size();
    }


    @Override
    public Candidate removeFromWaitingQueue(long candidateId, String jobuuid, String companyName, String positionName) {
        System.out.println("want to remove id " + candidateId);
        synchronized (queue) {
            Iterator<Candidate> it = queue.iterator();
            while (it.hasNext()) {
                Candidate candidate = it.next();
                System.out.println("find candidate with id " + candidate.getId());
                if (candidate.getId() == candidateId) {
                    System.out.println("REMOVE!!!!!!!!!!!!!!");
                    candidate.setJobqueue("");
                    it.remove();
                    publish(getJobsWaiting(), jobuuid, companyName, positionName);
                    System.out.println(queue.size());
                    return candidate;
                }
            }

        }
        return null;
    }

    private void publish(Collection<Candidate> candidateWaiting, String jobuuid, String companyName, String positionName) {
        int i = 1;
        System.out.println("try to publish $$$$$$$");
        for (Candidate c : candidateWaiting) {
            JSONObject userWSMessage = new JSONObject();
            userWSMessage.put(WS_MESSAGE_ACTION, WS_MESSAGE_ACTION_CHANGEPOSITION);
            userWSMessage.put(WS_MESSAGE_DATA, new JSONObject()
                    .put(WS_MESSAGE_DATA_POSITION, i)
                    .put(WS_MESSAGE_DATA_JOBID, jobuuid)
                    .put(WS_MESSAGE_DATA_COMPANYNAME, companyName)
                    .put(WS_MESSAGE_DATA_POSITIONNAME, positionName));
            WSClient.singleton().client.publish(WSClient.USERWSPREFIX + c.getUuid(), userWSMessage.toString());
            i++;
        }
    }

    @Override
    public void run() {
        System.out.println("I am here FIFOScheduler Run num of waiting " + getJobsWaiting());
//    synchronized (queue) {
//      while (terminate == false) {
//        synchronized (queue) {
//          if (runningJob != null || queue.isEmpty() == true) {
//            try {
//              queue.wait(500);
//            } catch (InterruptedException e) {
//              LOGGER.error("Exception in FIFOScheduler while run queue.wait", e);
//            }
//            continue;
//          }
//
//          runningJob = queue.remove(0);
//        }
//
//        final Scheduler scheduler = this;
//        this.executor.execute(new Runnable() {
//          @Override
//          public void run() {
//            if (runningJob.isAborted()) {
//              runningJob.setStatus(Status.ABORT);
//              runningJob.aborted = false;
//              synchronized (queue) {
//                queue.notify();
//              }
//              return;
//            }
//
//            runningJob.setStatus(Status.RUNNING);
//            if (listener != null) {
//              listener.jobStarted(scheduler, runningJob);
//            }
//            runningJob.run();
//            if (runningJob.isAborted()) {
//              runningJob.setStatus(Status.ABORT);
//            } else {
//              if (runningJob.getException() != null) {
//                runningJob.setStatus(Status.ERROR);
//              } else {
//                runningJob.setStatus(Status.FINISHED);
//              }
//            }
//            if (listener != null) {
//              listener.jobFinished(scheduler, runningJob);
//            }
//            // reset aborted flag to allow retry
//            runningJob.aborted = false;
//            runningJob = null;
//            synchronized (queue) {
//              queue.notify();
//            }
//          }
//        });
//      }
//    }
    }

    @Override
    public void stop() {
        terminate = true;
        synchronized (queue) {
            queue.notify();
        }
    }

}
