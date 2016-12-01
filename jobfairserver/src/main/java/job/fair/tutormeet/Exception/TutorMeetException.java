package job.fair.tutormeet.Exception;

import org.json.JSONObject;

/**
 * Created by annawang on 2/8/16.
 */
public class TutorMeetException extends RuntimeException {
    public TutorMeetException(JSONObject errorJson) {
        super(errorJson.toString());
    }

}
