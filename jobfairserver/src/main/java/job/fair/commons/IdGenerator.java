package job.fair.commons;

import java.util.UUID;

/**
 * Created by annawang on 2/22/16.
 */
public class IdGenerator {
    public static String createId() {
        UUID uuid = java.util.UUID.randomUUID();
        String s = Long.toString(uuid.getMostSignificantBits(), 24); //+ '-' +
        //Long.toString(uuid.getLeastSignificantBits(), 24);
        return s;
    }
}
