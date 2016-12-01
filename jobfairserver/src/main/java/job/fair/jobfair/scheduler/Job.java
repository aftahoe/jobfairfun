package job.fair.jobfair.scheduler;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public abstract class Job {
    static Logger LOGGER = LoggerFactory.getLogger(Job.class);
    String id;
    Object result;
    Date dateCreated;
    Date dateStarted;
    Date dateFinished;
    Status status;
    transient boolean aborted = false;
    String errorMessage;
    private String jobName;
    private transient Throwable exception;
    private transient JobListener listener;
    private long progressUpdateIntervalMs;

    public Job(String jobName, JobListener listener, long progressUpdateIntervalMs) {
        this.jobName = jobName;
        this.listener = listener;
        this.progressUpdateIntervalMs = progressUpdateIntervalMs;

        dateCreated = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        id = dateFormat.format(dateCreated) + "_" + super.hashCode();

        setStatus(Status.READY);
    }

    public Job(String jobName, JobListener listener) {
        this(jobName, listener, JobProgressPoller.DEFAULT_INTERVAL_MSEC);
    }

    public Job(String jobId, String jobName, JobListener listener, long progressUpdateIntervalMs) {
        this.jobName = jobName;
        this.listener = listener;
        this.progressUpdateIntervalMs = progressUpdateIntervalMs;

        id = jobId;

        setStatus(Status.READY);
    }

    public static String getStack(Throwable e) {
        if (e == null) {
            return "";
        }

        Throwable cause = ExceptionUtils.getRootCause(e);
        return ExceptionUtils.getFullStackTrace(cause);
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return ((Job) o).hashCode() == hashCode();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (this.status == status) {
            return;
        }
        Status before = this.status;
        Status after = status;
        if (listener != null) {
            listener.beforeStatusChange(this, before, after);
        }
        this.status = status;
        if (listener != null) {
            listener.afterStatusChange(this, before, after);
        }
    }

    public JobListener getListener() {
        return listener;
    }

    public void setListener(JobListener listener) {
        this.listener = listener;
    }

    public boolean isTerminated() {
        return !this.status.isReady() && !this.status.isRunning() && !this.status.isPending();
    }

    public boolean isRunning() {
        return this.status.isRunning();
    }

    public void run() {
        JobProgressPoller progressUpdator = null;
        try {
            progressUpdator = new JobProgressPoller(this, progressUpdateIntervalMs);
            progressUpdator.start();
            dateStarted = new Date();
            result = jobRun();
            this.exception = null;
            errorMessage = null;
            dateFinished = new Date();
            progressUpdator.terminate();
        } catch (NullPointerException e) {
            LOGGER.error("Job failed", e);
            progressUpdator.terminate();
            this.exception = e;
            result = e.getMessage();
            errorMessage = getStack(e);
            dateFinished = new Date();
        } catch (Throwable e) {
            LOGGER.error("Job failed", e);
            progressUpdator.terminate();
            this.exception = e;
            result = e.getMessage();
            errorMessage = getStack(e);
            dateFinished = new Date();
        } finally {
            //aborted = false;
        }
    }

    public Throwable getException() {
        return exception;
    }

    protected void setException(Throwable t) {
        exception = t;
        errorMessage = getStack(t);
    }

    public Object getReturn() {
        return result;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public abstract int progress();

    public abstract Map<String, Object> info();

    protected abstract Object jobRun() throws Throwable;

    protected abstract boolean jobAbort();

    public void abort() {
        aborted = jobAbort();
    }

    public boolean isAborted() {
        return aborted;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public Date getDateFinished() {
        return dateFinished;
    }

    protected void setResult(Object result) {
        this.result = result;
    }

    /**
     * Job status.
     * <p>
     * READY - Job is not running, ready to run.
     * PENDING - Job is submitted to scheduler. but not running yet
     * RUNNING - Job is running.
     * FINISHED - Job finished run. with success
     * ERROR - Job finished run. with error
     * ABORT - Job finished by abort
     */
    public static enum Status {
        READY,
        PENDING,
        RUNNING,
        FINISHED,
        ERROR,
        ABORT;

        public boolean isReady() {
            return this == READY;
        }

        public boolean isRunning() {
            return this == RUNNING;
        }

        public boolean isPending() {
            return this == PENDING;
        }
    }
}
