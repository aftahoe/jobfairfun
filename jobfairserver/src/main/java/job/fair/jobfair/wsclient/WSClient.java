package job.fair.jobfair.wsclient;

import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Job;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.util.concurrent.TimeUnit;

import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_ACTION;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_ACTION_CHANGESTATUS;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_STATUS;
import static job.fair.jobfair.JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERID;

/**
 * Created by annawang on 2/17/16.
 */
//TODO maybe we need a WSClient factory
public class WSClient {
    public static final String JOBWSPREFIX = "job.";
    public static final String USERWSPREFIX = "user.";

    private static final Logger logger = LoggerFactory.getLogger(WSClient.class);
    public static WampClient client;
    private static WSClient singleton;
    private static Long singletonLock = new Long(0);

    public WSClient() throws Exception {
        // Build the client
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        try {
            builder.withConnectorProvider(connectorProvider)
                    .withUri("ws://172.16.8.147:9090/ws1")
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(3, TimeUnit.SECONDS);
            client = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        client.statusChanged().subscribe(new Action1<WampClient.State>() {
            @Override
            public void call(WampClient.State t1) {
                System.out.println("Session1 status changed to " + t1);

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable t) {
                System.out.println("Session1 ended with error " + t);
            }
        }, new Action0() {
            @Override
            public void call() {
                System.out.println("Session1 ended normally");
            }
        });

        client.open();
        Thread.sleep(5000);
    }

    public static WSClient singleton() {
        if (singleton == null) {
            synchronized (singletonLock) {
                if (singleton == null) {
                    try {
                        singleton = new WSClient();
                    } catch (Exception e) {
                        logger.error(e.toString(), e);
                    }
                }
            }
        }
        return singleton;
    }

    public static void publishStatusChange(Candidate candidate, Job job) {

        long joblongid = Long.parseLong(candidate.getJobqueue().substring(8));
        //publish
        JSONObject jobWSMessage = new JSONObject();
        jobWSMessage.put(WS_MESSAGE_ACTION, WS_MESSAGE_ACTION_CHANGESTATUS);
        jobWSMessage.put(WS_MESSAGE_DATA, new JSONObject()
                .put(WS_MESSAGE_DATA_USERID, candidate.getUuid())
                .put(WS_MESSAGE_DATA_STATUS, candidate.getStatus().getOrdinal()));
        WSClient.singleton().client.publish(WSClient.JOBWSPREFIX + job.getUuid(), jobWSMessage.toString());


    }
}
