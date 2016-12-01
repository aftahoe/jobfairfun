package job.fair.jobfair.jpa.websocket;

import job.fair.commons.ObjectConverter;
import job.fair.jobfair.JobfairConstants;
import job.fair.jobfair.jpa.controller.CandidateController;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.testutil.TestUtil;
import job.fair.jobfair.wsclient.WSClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import rx.functions.Action0;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by annawang on 2/17/16.
 */
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("jpa")
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class WebSocketMessageTest {
    public static final String TEST_SESSION_ID = "testSessionId";
    public static final String TEST_COMPANYNAME_VALUE = "testcompany";
    private static final String TEST_JOB_UUID = "job1uuid";
    private static final String TEST_USER2_EMAIL = "pCNBnv@gmail.com";
    private static final String TEST_USER2_PASSWORD = "55ReQFZX";
    // WS fields
    private static WampClient client2;
    private static String getJobMessage;
    private static String getUserMessage1;
    private static String getUserMessage2;

    private static String user1UUID = "user1UUID";
    private static String user2UUID = "user2UUID";
    private static String user1RealUUID;
    private static String user2RealUUID;


    @Autowired
    protected CompanyService companyService;
    @Autowired
    private CandidateController candidateController;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MockHttpSession mockHttpSession;
    @Autowired
    private MockHttpServletRequest request;
    private MockMvc mockMvc;
    private CharacterEncodingFilter characterEncodingFilter;


    @Before
    public void setup() throws Exception {
        characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();
        mockHttpSession = new MockHttpSession(context.getServletContext(), TEST_SESSION_ID);
        mockHttpSession.setAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE, "{\"jobfairUserId\":1," +
                "\"sessionId\":\"testSessionId\"," +
                "\"jobfairUserUUID\":\"user1UUID\"," +
                "\"userId\":\"0\"," +
                "\"email\":\"" + TestUtil.TUTORMEET_TEST_EMAIL_VALUE + "\"," +
                "\"avatar\":\"0\"}");

    }

    private void secondUser() throws Exception {
        characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();
        mockHttpSession = new MockHttpSession(context.getServletContext(), TEST_SESSION_ID);
        mockHttpSession.setAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE, "{\"jobfairUserId\":2," +
                "\"sessionId\":\"testSessionId\"," +
                "\"jobfairUserUUID\":\"user2UUID\"," +
                "\"userId\":\"0\"," +
                "\"email\":\"" + TEST_USER2_EMAIL + "\"," +
                "\"avatar\":\"0\"}");

    }

    private void interviewer() throws Exception {
        characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();
        mockHttpSession = new MockHttpSession(context.getServletContext(), TEST_SESSION_ID);
        mockHttpSession.setAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE, "{\"jobfairCompanyId\":1," +
                "\"jobfairUserId\":1," +
                "\"sessionId\":\"testSessionId\"," +
                "\"jobfairUserUUID\":\"interviewerUUID\"," +
                "\"userId\":\"0\"," +
                "\"email\":\"" + TestUtil.TUTORMEET_TEST_EMAIL_VALUE + "\"," +
                "\"avatar\":\"0\"}");
    }

    @Test
    public void candidateLoginAndCreateCompany() throws Exception {
        Candidate c = TestUtil.createTutorMeetCandidate();
        c.setUuid(user1UUID);
        mockHttpSession.setNew(true);
        MvcResult login1 = mockMvc.perform(post("/candidate/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(c)))
                .andExpect(status().isOk())
                .andReturn();
        JSONObject login1JSON = new JSONObject(login1.getResponse().getContentAsString());
        user1RealUUID = login1JSON.getString("id");


        secondUser();
        Candidate c2 = new Candidate();
        c2.setUuid(user2UUID);
        c2.setEmail(TEST_USER2_EMAIL);
        c2.setPassword(TEST_USER2_PASSWORD);
        mockHttpSession.setNew(true);
        MvcResult login2 = mockMvc.perform(post("/candidate/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(c2)))
                .andExpect(status().isOk())
                .andReturn();
        JSONObject login2JSON = new JSONObject(login2.getResponse().getContentAsString());
        user2RealUUID = login2JSON.getString("id");


        interviewer();
        Interviewer in = new Interviewer();
        in.setUuid("interviewerUUID");
        in.setEmail(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        in.setPassword(TestUtil.TUTORMEET_TEST_PASSWORD);

        mockHttpSession.setNew(true);
        mockMvc.perform(post("/interviewer/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(in)))
                .andExpect(status().isOk())
                .andReturn();

        //DO nothing
        createCompany();

        buildWSClient();

    }


    @Test
    public void candidateLoginApply() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/candidate/apply/" + TEST_JOB_UUID)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication()))
                .param("questionAnswers", candidateAnswers().toString()))
                .andExpect(status().isOk());

        Thread.sleep(5000);
        JSONObject userMessage = new JSONObject(getUserMessage1);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_ADD, userMessage.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(1, userMessage.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getInt(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITION));

        JSONObject jobMessage = new JSONObject(getJobMessage);
        System.out.println(jobMessage);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_ADD, jobMessage.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        JSONObject userEntry = jobMessage.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERENTRY);
        Assert.assertEquals(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, userEntry.getString("email"));
        assertTrue(userEntry.getDouble("score") >= 0);
        assertTrue(userEntry.getDouble("score") <= 1);
        assertEquals(0, userEntry.getInt("status"));
    }

    @Test
    public void candidateLoginApply2() throws Exception {
        secondUser();
        String candidateJSONString = ObjectConverter.toJSONString(TestUtil.createApplication());

        JSONArray answers = candidateAnswers();

        byte[] content = "this is the Preload51job resume content".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("resume", "resume.txt", "text/plain", content);


        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/candidate/apply/" + TEST_JOB_UUID)
                .file(mockMultipartFile)
                .session(mockHttpSession)
                .param("candidate", candidateJSONString)
                .param("questionAnswers", answers.toString()))
                .andExpect(status().isOk());

        Thread.sleep(5000);
        JSONObject userMessage = new JSONObject(getUserMessage2);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_ADD, userMessage.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(2, userMessage.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getInt(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITION));

        JSONObject jobMessage = new JSONObject(getJobMessage);
        System.out.println(jobMessage);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_ADD, jobMessage.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        JSONObject userEntry = jobMessage.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERENTRY);
        assertEquals(TEST_USER2_EMAIL, userEntry.getString("email"));
        assertTrue(userEntry.getDouble("score") >= 0);
        assertTrue(userEntry.getDouble("score") <= 1);
        assertEquals(0, userEntry.getInt("status"));


    }

    @Test
    public void candidateStatusChange() throws Exception {
        JSONObject status = new JSONObject();
        status.put("status", 1);
        mockMvc.perform(put("/candidate/status")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(status.toString().getBytes()));

        Thread.sleep(5000);
        JSONObject json = new JSONObject(getJobMessage);

        assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_CHANGESTATUS, json.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(user1RealUUID, json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString("userid"));
        assertEquals(1, json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getInt("status"));

    }

    @Test
    public void testDeletebyInterviewer() throws Exception {
        interviewer();
        mockMvc.perform(delete("/interviewer/job/" + TEST_JOB_UUID + "/candidates/" + user1RealUUID).session(mockHttpSession))
                .andExpect(status().isOk());

        Thread.sleep(5000);

        JSONObject json = new JSONObject(getJobMessage);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE, json.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(user1RealUUID, json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERID));
        Assert.assertEquals(User.Type.INTERVIEWER.name(), json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY));

        JSONObject user1 = new JSONObject(getUserMessage1);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE, user1.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(TEST_JOB_UUID, user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID));
        assertEquals(User.Type.INTERVIEWER.name(), user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY));
        assertEquals(TEST_COMPANYNAME_VALUE, user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME));
        assertEquals("Front-End Developer", user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME));

        JSONObject user2 = new JSONObject(getUserMessage2);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_CHANGEPOSITION, user2.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(1, user2.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getInt(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITION));

    }

    @Test
    public void testDeletebyUser() throws Exception {
        secondUser();
        mockMvc.perform(delete("/candidate/apply/" + TEST_JOB_UUID).session(mockHttpSession))
                .andExpect(status().isOk());

        Thread.sleep(5000);

        JSONObject json = new JSONObject(getJobMessage);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE, json.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(user2UUID, json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERID));
        assertEquals(User.Type.CANDIDATE.name(), json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY));

        JSONObject user2 = new JSONObject(getUserMessage2);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE, user2.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(TEST_JOB_UUID, user2.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID));
        assertEquals(User.Type.CANDIDATE.name(), user2.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY));
        assertEquals(TEST_COMPANYNAME_VALUE, user2.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME));
        assertEquals("Front-End Developer", user2.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME));
    }

//    @Test
//    public void testInviteInterview() throws Exception {
//        // Make sure user available
//        JSONObject status = new JSONObject();
//        status.put("status", 0);
//        mockMvc.perform(put("/candidate/status")
//                .session(mockHttpSession)
//                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
//                .content(status.toString().getBytes()));
//
//        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/candidate/apply/" + TEST_JOB_UUID)
//                .file(TestUtil.testResumeMultipartFile())
//                .session(mockHttpSession)
//                .param("candidate", TestUtil.getAppliedCandidateJSONString())
//                .param("questionAnswers", candidateAnswers().toString()))
//                .andExpect(status().isOk());
//
//        interviewer();
//        mockMvc.perform(get("/interviewer/launchinterview")
//                .param("jobid", TEST_JOB_UUID)
//                .param("candidateid", user1RealUUID)
//                .session(mockHttpSession))
//                .andExpect(status().isOk());
//
//        Thread.sleep(5000);
//
//        JSONObject user1 = new JSONObject(getUserMessage1);
//        assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_INTERVIEW, user1.getString(WS_MESSAGE_ACTION));
//        assertEquals(TEST_JOB_UUID, user1.getJSONObject(WS_MESSAGE_DATA).getString(WS_MESSAGE_DATA_JOBID));
//        assertEquals(TEST_COMPANYNAME_VALUE, user1.getJSONObject(WS_MESSAGE_DATA).getString(WS_MESSAGE_DATA_COMPANYNAME));
//        assertEquals("Front-End Developer", user1.getJSONObject(WS_MESSAGE_DATA).getString(WS_MESSAGE_DATA_POSITIONNAME));
//        assertTrue(user1.getJSONObject(WS_MESSAGE_DATA).getString(WS_MESSAGE_DATA_ATTENDEE_MEETINGURL)
//                .contains(new TutorMeet().MEETING_URL_PREFIX));
//
//        JSONObject json = new JSONObject(getJobMessage);
//        assertEquals(WS_MESSAGE_ACTION_CHANGESTATUS, json.getString(WS_MESSAGE_ACTION));
//        assertEquals(user1RealUUID, json.getJSONObject(WS_MESSAGE_DATA).getString(WS_MESSAGE_DATA_USERID));
//        assertEquals(2, json.getJSONObject(WS_MESSAGE_DATA).getInt(WS_MESSAGE_DATA_STATUS));
//    }

    @Test
    public void testPostEvaluationReport() throws Exception {
        EvaluationReport report = new EvaluationReport();
        report.setNote("this is test note");
        report.setRecommendationType(EvaluationReport.Type.RECOMMENDED);
        report.setJobId(TEST_JOB_UUID);
        report.setCandidateId(user1RealUUID);

        Map<String, Integer> scores = new HashMap<>();
        scores.put(TestUtil.EVALUATION_UUID_1, 1);
        scores.put(TestUtil.EVALUATION_UUID_2, 4);
        scores.put(TestUtil.EVALUATION_UUID_3, 7);
        scores.put(TestUtil.EVALUATION_UUID_4, 10);
        report.setScores(scores);
        interviewer();
        mockMvc.perform(post("/interviewer/evaluation")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(ObjectConverter.toJSONString(report)))
                .andExpect(status().isOk())
                .andReturn();

        JSONObject json = new JSONObject(getJobMessage);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE, json.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(user1RealUUID, json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_USERID));
        assertEquals(User.Type.INTERVIEW.name(), json.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY));

        JSONObject user1 = new JSONObject(getUserMessage1);
        Assert.assertEquals(JobfairConstants.WSMessages.WS_MESSAGE_ACTION_REMOVE, user1.getString(JobfairConstants.WSMessages.WS_MESSAGE_ACTION));
        assertEquals(TEST_JOB_UUID, user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_JOBID));
        assertEquals(User.Type.INTERVIEW.name(), user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_REMOVEBY));
        assertEquals(TEST_COMPANYNAME_VALUE, user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_COMPANYNAME));
        assertEquals("Front-End Developer", user1.getJSONObject(JobfairConstants.WSMessages.WS_MESSAGE_DATA).getString(JobfairConstants.WSMessages.WS_MESSAGE_DATA_POSITIONNAME));
    }

    private JSONArray candidateAnswers() {
        JSONArray answers = new JSONArray();
        JSONObject answer1 = new JSONObject();
        answer1.put("questionId", "question1uuid");
        answer1.put("answer", 1);
        answers.put(answer1);
        JSONObject answer2 = new JSONObject();
        answer2.put("questionId", "question2uuid");
        answer2.put("answer", 3);
        answers.put(answer2);
        return answers;
    }

    private void createCompany() {
        Company company = TestUtil.createTestCompany();
        company.setName(TEST_COMPANYNAME_VALUE);
        company.setLink("testlink");

        companyService.addCompany(company);

    }


    private void buildWSClient() throws InterruptedException {
        // Build subscripted client
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();


        try {
            builder.withConnectorProvider(connectorProvider)
                    .withUri("ws://172.16.8.147:9090/ws1")
                    .withRealm("realm1")
                    .withInfiniteReconnects()
                    .withReconnectInterval(3, TimeUnit.SECONDS);
            client2 = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        client2.statusChanged().subscribe(new Action1<WampClient.State>() {
            @Override
            public void call(WampClient.State t1) {
                System.out.println("Session2 status changed to " + t1);

                if (t1 instanceof WampClient.ConnectedState) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }

                    client2.makeSubscription(WSClient.JOBWSPREFIX + TEST_JOB_UUID, String.class)
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String t1) {
                                    System.out.println("Received value from job ws " + t1);
                                    getJobMessage = t1;
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable t1) {
                                    System.out.println("Completed event Preload51job.event with error " + t1);
                                }
                            }, new Action0() {
                                @Override
                                public void call() {
                                    System.out.println("Completed event Preload51job.event");
                                }
                            });
                    // Subscribe on the topic

                    client2.makeSubscription(WSClient.USERWSPREFIX + user1UUID, String.class)
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String t1) {
                                    System.out.println("Received value from user1 uuid " + t1);
                                    getUserMessage1 = t1;
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable t1) {
                                    System.out.println("Completed event Preload51job.event with error " + t1);
                                }
                            }, new Action0() {
                                @Override
                                public void call() {
                                    System.out.println("Completed event Preload51job.event");
                                }
                            });

                    client2.makeSubscription(WSClient.USERWSPREFIX + user1RealUUID, String.class)
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String t1) {
                                    System.out.println("Received value from user1 real uuid " + t1);
                                    getUserMessage1 = t1;
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable t1) {
                                    System.out.println("Completed event Preload51job.event with error " + t1);
                                }
                            }, new Action0() {
                                @Override
                                public void call() {
                                    System.out.println("Completed event Preload51job.event");
                                }
                            });

                    client2.makeSubscription(WSClient.USERWSPREFIX + user2UUID, String.class)
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String t1) {
                                    System.out.println("Received value from user2 uuid " + t1);
                                    getUserMessage2 = t1;
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable t1) {
                                    System.out.println("Completed event Preload51job.event with error " + t1);
                                }
                            }, new Action0() {
                                @Override
                                public void call() {
                                    System.out.println("Completed event Preload51job.event");
                                }
                            });

                    client2.makeSubscription(WSClient.USERWSPREFIX + user2RealUUID, String.class)
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String t1) {
                                    System.out.println("Received value from user2 real uuid " + t1);
                                    getUserMessage2 = t1;
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable t1) {
                                    System.out.println("Completed event Preload51job.event with error " + t1);
                                }
                            }, new Action0() {
                                @Override
                                public void call() {
                                    System.out.println("Completed event Preload51job.event");
                                }
                            });
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable t) {
                System.out.println("Session2 ended with error " + t);
            }
        }, new Action0() {
            @Override
            public void call() {
                System.out.println("Session2 ended normally");
            }
        });
        client2.open();
        Thread.sleep(7000);
    }

}
