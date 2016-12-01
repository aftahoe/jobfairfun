package job.fair.jobfair.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by annawang on 2/5/16.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnknownMatchException extends RuntimeException {
    public UnknownMatchException(String matchId) {
        super("Unknown match: " + matchId);
    }
}