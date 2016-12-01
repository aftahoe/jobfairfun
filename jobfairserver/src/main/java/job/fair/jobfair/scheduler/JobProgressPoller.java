package job.fair.jobfair.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobProgressPoller extends Thread {
    public static final long DEFAULT_INTERVAL_MSEC = 500;
    Logger logger = LoggerFactory.getLogger(JobProgressPoller.class);
    boolean terminate = false;
    private Job job;
    private long intervalMs;

    public JobProgressPoller(Job job, long intervalMs) {
        this.job = job;
        this.intervalMs = intervalMs;
    }

    @Override
    public void run() {
        if (intervalMs < 0) {
            return;
        } else if (intervalMs == 0) {
            intervalMs = DEFAULT_INTERVAL_MSEC;
        }

        while (terminate == false) {
            JobListener listener = job.getListener();
            if (listener != null) {
                try {
                    if (job.isRunning()) {
                        listener.onProgressUpdate(job, job.progress());
                    }
                } catch (Exception e) {
                    logger.error("Can not get or update progress", e);
                }
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                logger.error("Exception in JobProgressPoller while run Thread.sleep", e);
            }
        }
    }

    public void terminate() {
        terminate = true;
    }
}
