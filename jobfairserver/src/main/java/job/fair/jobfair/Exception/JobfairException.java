package job.fair.jobfair.Exception;

/**
 * Created by annawang on 2/5/16.
 */
public class JobfairException extends RuntimeException {
    public JobfairException(String matchId) {
        super(matchId);
    }
}