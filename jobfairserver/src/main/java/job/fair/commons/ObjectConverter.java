package job.fair.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

/**
 * Created by annawang on 2/27/16.
 */
public class ObjectConverter {

    private static ObjectMapper mapper = new ObjectMapper();

    public static JSONObject toJSONObject(Object o) throws JsonProcessingException {
        return new JSONObject(mapper.writeValueAsString(o));
    }

    public static String toJSONString(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }
}
