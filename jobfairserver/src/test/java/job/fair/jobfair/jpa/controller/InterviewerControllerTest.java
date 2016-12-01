package job.fair.jobfair.jpa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import job.fair.commons.ObjectConverter;
import job.fair.data.Page;
import job.fair.jobfair.JobfairConstants;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.InterviewerRepository;
import job.fair.jobfair.jpa.service.CandidateService;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.jpa.service.UserService;
import job.fair.jobfair.scheduler.SchedulerFactory;
import job.fair.jobfair.testutil.TestUtil;
import job.fair.tutormeet.TutorMeet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by annawang on 2/24/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@WebAppConfiguration
//@TransactionConfiguration(defaultRollback = false)
//@Transactional
@ActiveProfiles("jpa")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InterviewerControllerTest {

    private static String interviewerUUID;
    private static String candidate1UUID;
    private static String candidate2UUID;
    private static String apiToken;
    @Autowired
    private CompanyController compController;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private InterviewerRepository interviewerRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    @Autowired
    private MockHttpSession mockHttpSession;
    @Autowired
    private MockHttpServletRequest request;
    private CharacterEncodingFilter characterEncodingFilter;

    @Before
    public void setup() throws Exception {
        characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();
        mockHttpSession = new MockHttpSession(context.getServletContext(), "testSessionId");
        mockHttpSession.setAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE,
                "{\"jobfairUserId\":1," +
                        "\"sessionId\":\"testSessionId\"," +
                        "\"jobfairUserUUID\":\"testuseruuid\"," +
                        "\"userId\":\"24\"," +
                        "\"avatar\":\"1\"," +
                        "\"jobfairCompanyId\":\"1\"," +
                        "\"email\":\"interviewer@gmail.com\"," +
                        "\"apiToken\":\"" + apiToken + "\"}");
    }

    // ============= login  ===================
    @Test
    public void login() throws Exception {
        mockHttpSession.setNew(true);
        MvcResult result = mockMvc.perform(post("/interviewer/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(TestUtil.createLoginInterviewer())))
                .andExpect(status().isOk())
                .andReturn();

        // The session after login
        JSONObject userSessionJSON = new JSONObject(
                (String) result.getRequest().getSession(false).getAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE));


        // Make sure all information we care are in the session object
        assertEquals(0, userSessionJSON.getInt(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_RETURNCODE));
        apiToken = userSessionJSON.getString(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_APITOKEN);
        Assert.hasLength(apiToken);
        Assert.hasLength(userSessionJSON.getString(JobfairConstants.SessionParms.SESSION_ID));
        Assert.hasLength(userSessionJSON.getString(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_EMAIL));
        Assert.hasLength(userSessionJSON.getString(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_NAME));
        interviewerUUID = userSessionJSON.getString(JobfairConstants.SessionParms.JOBFAIR_USER_UUID);
        Assert.hasLength(interviewerUUID);
        assertEquals(1L, userSessionJSON.getLong(JobfairConstants.SessionParms.JOBFAIR_USER_ID));
        assertEquals(24, userSessionJSON.getInt(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_USERSN));
        assertEquals(1, userSessionJSON.getInt(JobfairConstants.SessionParms.JOBFAIR_COMPANY_ID));
        org.junit.Assert.assertEquals(User.Type.INTERVIEWER.name(), userSessionJSON.getString(JobfairConstants.SessionParms.JOBFAIR_ROLE));


        // Return object
        JSONObject resultJSON = new JSONObject(result.getResponse().getContentAsString());
        JSONObject expectedJSON = new JSONObject()
                .put("id", interviewerUUID)
                .put("email", TestUtil.TUTORMEET_TEST_EMAIL_VALUE)
                .put("name", TestUtil.TUTORMEET_TEST_NAME_VALUE)
                .put("role", User.Type.INTERVIEWER.name())
                .put("avatar", "24122")
                .put("iconPathPrefix", new TutorMeet().TUTORMEET_AVATAR_PREFIX + "24_");
        JSONAssert.assertEquals(expectedJSON, resultJSON, true);
    }

    @Test
    public void loginFailed() throws Exception {
        Interviewer in = TestUtil.createLoginInterviewer();
        in.setPassword("failedpassword");

        mockMvc.perform(post("/interviewer/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(in)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.url").value(containsString("/interviewer/login")));
    }

    @Test
    public void loginWithoutEmail() throws Exception {
        Interviewer in = TestUtil.createLoginInterviewer();
        in.setEmail("");
        mockMvc.perform(post("/interviewer/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(in)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Interviewer's email is required")));
    }

    @Test
    public void loginWithNullEmail() throws Exception {
        Interviewer in = new Interviewer();

        mockMvc.perform(post("/interviewer/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(in)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Interviewer's email is required")));
    }

    @Test
    public void loginWithoutPassword() throws Exception {
        Interviewer in = TestUtil.createLoginInterviewer();
        in.setPassword("");
        mockMvc.perform(post("/interviewer/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(in)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Interviewer's password is required")));
    }

    @Test
    public void loginWithNullPassword() throws Exception {
        Interviewer in = new Interviewer();
        in.setEmail("aaa@gmail.com");

        mockMvc.perform(post("/interviewer/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(in)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Interviewer's password is required")));
    }

    //============== logout tests=====================
    @Test
    public void logout() throws Exception {
        MvcResult result = mockMvc.perform(post("/interviewer/logout")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // make sure the session is set to null
        assertTrue(result.getRequest().getSession(false) == null);

        // make sure the message is correct
        String messageString = new JSONObject(result.getResponse().getContentAsString()).getString("message");
        assertEquals("logout success!", messageString);

        //make sure the candidate status to Away
        CandidateService mockedCandidateServer = mock(CandidateService.class);
        CandidateController controller = withMockedServices(mockedCandidateServer, null);
        controller.candidateLogout();
        Mockito.verify(mockedCandidateServer, times(1)).updateCandidateStatus(
                new Candidate(1L, "testuseruuid", Candidate.CandidateStatus.AWAY));
    }

    @Test
    public void logoutFailed() throws Exception {
        MvcResult result = mockMvc.perform(post("/interviewer/logout")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String messageString = new JSONObject(result.getResponse().getContentAsString()).getString("message");
        assertEquals("Session Authentication Failed: Session is Null", messageString);
    }

    //==================== get all interviewer jobs ==========================
    @Test
    public void getInterviewerJobs() throws Exception {

        MvcResult result = mockMvc.perform(get("/interviewer/jobs").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray resultJSON = new JSONArray(result.getResponse().getContentAsString());
        System.out.println(resultJSON.toString());
        JSONArray expectedJSONArray = new JSONArray();
        JSONObject com1 = new JSONObject()
                .put("id", TestUtil.JOB1_UUID)
                .put("positionNumber", 0)
                .put("positionName", TestUtil.JOB1_POSITION_NAME)
                .put("positionCount", TestUtil.JOB1_POSITION_COUNT)
                .put("candidateWaitingCount", 2)
                .put("recommendationCount", 0);
        expectedJSONArray.put(com1);
        JSONObject com2 = new JSONObject()
                .put("id", TestUtil.JOB2_UUID)
                .put("positionNumber", 0)
                .put("positionName", TestUtil.JOB2_POSITION_NAME)
                .put("positionCount", TestUtil.JOB2_POSITION_COUNT)
                .put("candidateWaitingCount", 0)
                .put("recommendationCount", 0);
        expectedJSONArray.put(com2);
        JSONObject com3 = new JSONObject()
                .put("id", TestUtil.JOB3_UUID)
                .put("positionNumber", 0)
                .put("positionName", TestUtil.JOB3_POSITION_NAME)
                .put("positionCount", TestUtil.JOB3_POSITION_COUNT)
                .put("candidateWaitingCount", 0)
                .put("recommendationCount", 0);
        expectedJSONArray.put(com3);
        JSONObject com4 = new JSONObject()
                .put("id", TestUtil.JOB4_UUID)
                .put("positionNumber", 0)
                .put("positionName", TestUtil.JOB4_POSITION_NAME)
                .put("positionCount", TestUtil.JOB4_POSITION_COUNT)
                .put("candidateWaitingCount", 0)
                .put("recommendationCount", 0);
        expectedJSONArray.put(com4);
        JSONObject com5 = new JSONObject()
                .put("id", TestUtil.JOB5_UUID)
                .put("positionNumber", 0)
                .put("positionName", TestUtil.JOB5_POSITION_NAME)
                .put("positionCount", TestUtil.JOB5_POSITION_COUNT)
                .put("candidateWaitingCount", 0)
                .put("recommendationCount", 0);
        expectedJSONArray.put(com5);
        JSONObject com6 = new JSONObject()
                .put("id", TestUtil.JOB6_UUID)
                .put("positionNumber", 0)
                .put("positionName", TestUtil.JOB6_POSITION_NAME)
                .put("positionCount", TestUtil.JOB6_POSITION_COUNT)
                .put("candidateWaitingCount", 0)
                .put("recommendationCount", 0);
        expectedJSONArray.put(com6);
        JSONAssert.assertEquals(expectedJSONArray, resultJSON, true);

    }

    //==================== get all waiting candidates in the job ==========================
    @Test
    public void getCandidatesInJob_empty() throws Exception {
        MvcResult result = mockMvc.perform(get("/interviewer/job/job1uuid/candidates").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray resultJSON = new JSONArray(result.getResponse().getContentAsString());
        JSONArray expectedJSONArray = new JSONArray();

        JSONAssert.assertEquals(expectedJSONArray, resultJSON, true);

    }

    @Test
    public void getCandidatesInJob_some() throws Exception {
        candidatesLoginAndApply();
        MvcResult result = mockMvc.perform(get("/interviewer/job/job1uuid/candidates").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray resultJSON = new JSONArray(result.getResponse().getContentAsString());

        resultJSON.forEach(j -> {
            ((JSONObject) j).remove("id");
        });
        System.out.println(resultJSON.toString());

        JSONArray expectedJSONArray = new JSONArray();
        JSONObject c1 = new JSONObject()
                .put("iconPathPrefix", "https://corpqa.tutormeet.com/data/users/avatar/877_")
                .put("avatar", "1")
                .put("experience", 3)
                .put("salary", 100000)
                .put("skills", new JSONArray().put("JAVA").put("HTML"))
                .put("workethic", "my work ethic")
                .put("name", "SaqfUciDz8KAzNd2H6uFK")
                .put("cellphone", "my cell phone")
                .put("wechatid", "my we chat id")
                .put("score", 0.5)
                .put("email", "pCNBnv@gmail.com")
                .put("introduction", "my introduction")
                .put("status", 0);
        expectedJSONArray.put(c1);
        JSONObject c2 = new JSONObject()
                .put("iconPathPrefix", "https://corpqa.tutormeet.com/data/users/avatar/878_")
                .put("avatar", "")
                .put("experience", 3)
                .put("salary", 100000)
                .put("skills", new JSONArray().put("JAVA").put("HTML"))
                .put("workethic", "my work ethic")
                .put("name", "T4gu37zMJliaV5UFT")
                .put("cellphone", "my cell phone")
                .put("wechatid", "my we chat id")
                .put("score", 0.5)
                .put("email", "CBfDuY@gmail.com")
                .put("introduction", "my introduction")
                .put("status", 0);
        expectedJSONArray.put(c2);
        JSONAssert.assertEquals(expectedJSONArray, resultJSON, true);
    }

    //====================/launchinterview
    @Test
    public void testLaunchInterview() throws Exception {
        mockMvc.perform(get("/interviewer/launchinterview").session(mockHttpSession)
                .param("candidateid", candidate1UUID)
                .param("jobid", "job1uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hostMeetingURL").value(containsString(new TutorMeet().MEETING_URL_PREFIX)));
    }

    @Test
    public void testPutEvaluationReportAfterInterview() throws Exception {
        EvaluationReport report = new EvaluationReport();
        report.setNote("this is test note");
        report.setRecommendationType(EvaluationReport.Type.RECOMMENDED);
        report.setJobId("job1uuid");
        report.setCandidateId(candidate1UUID);

        Map<String, Integer> scores = new HashMap<>();
        scores.put(TestUtil.EVALUATION_UUID_1, 1);
        scores.put(TestUtil.EVALUATION_UUID_2, 4);
        scores.put(TestUtil.EVALUATION_UUID_3, 7);
        scores.put(TestUtil.EVALUATION_UUID_4, 10);
        report.setScores(scores);
        Interviewer in = this.interviewerRepository.getInterviewerByEmail(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        MvcResult result = mockMvc.perform(post("/interviewer/evaluation")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(ObjectConverter.toJSONString(report)))
                .andExpect(status().isOk())
                .andReturn();

        //TODO add tests
        Page<EvaluationReport> reportInDB = this.companyRepository.getEvaluationReport(in.getId(), null);
        assertEquals(1, reportInDB.getTotalElements());
        assertEquals(candidate1UUID, reportInDB.iterator().next().getApplication().getCandidate().getUuid());
        assertEquals(1, SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue" + 1).getJobsWaiting().size());
        Candidate c = this.candidateRepository.getCandidate(candidate1UUID);
        assertEquals(Candidate.CandidateStatus.AVAILABLE, c.getStatus());
    }

    @Test
    public void testPutGetEvaluationReport() throws Exception {
        MvcResult result = mockMvc.perform(get("/interviewer/recommended/candidates").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray reports = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, reports.length());
        assertEquals(4, ((JSONObject) reports.get(0)).getJSONObject("scores").length());

        //TODO what do we expect to return?
    }
    //===================/job/{jobuuid}/candidates/{candidateuuid}

    @Test
    public void testRemoveCandidateFromJobqueue() throws Exception {

        MvcResult result = mockMvc.perform(delete("/interviewer/job/job1uuid/candidates/" + candidate2UUID)
                .session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray jobsArray = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(0, jobsArray.length());
//TODO remaining candidate?
//        JSONObject resultJSON = jobsArray.getJSONObject(0);
//        System.out.println(resultJSON.toString());
//        JSONObject expectedJSONObject = new JSONObject();
//        expectedJSONObject.put("id", candidate1UUID);
//        expectedJSONObject.put("email",  "pCNBnv@gmail.com");
//        expectedJSONObject.put("name", "SaqfUciDz8KAzNd2H6uFK");
//        expectedJSONObject.put("avatar", "1");
//        expectedJSONObject.put("introduction", "my introduction");
//        expectedJSONObject.put("workethic", "my work ethic");
//        expectedJSONObject.put("cellphone", "my cell phone");
//        expectedJSONObject.put("wechatid", "my we chat id");
//        expectedJSONObject.put("experience", 3);
//        expectedJSONObject.put("salary", 100000);
//        expectedJSONObject.put("status", 0);
//        expectedJSONObject.put("skills", new JSONArray().put("JAVA").put("HTML"));
//        expectedJSONObject.put("jobscore", 0.5);
//        expectedJSONObject.put("iconPathPrefix", "https://corpqa.tutormeet.com/data/users/avatar/877_");

//        JSONAssert.assertEquals(expectedJSONObject, resultJSON, true);
        assertEquals(0, SchedulerFactory.singleton().createOrGetFIFOScheduler("jobqueue" + 1).getJobsWaiting().size());
    }

    // =================== evaluation
    @Test
    public void evaluationQuestionsGet_EmptyResult() throws Exception {
        MvcResult result = mockMvc.perform(get("/interviewer/evaluation").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray resultJSON = new JSONArray(result.getResponse().getContentAsString());
        JSONArray expectedJSONArray = new JSONArray();

        JSONAssert.assertEquals(expectedJSONArray, resultJSON, true);

    }

    @Test
    public void evaluationQuestionsGet_WithQuestions() throws Exception {
        Company company = TestUtil.createTestCompany();
        company.setEmailDomain("tutormeet.com");
        companyService.addCompany(company);

        MvcResult result = mockMvc.perform(get("/interviewer/evaluation").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();


        JSONArray resultJSON = new JSONArray(result.getResponse().getContentAsString());

        ObjectMapper mapper = new ObjectMapper();
        JSONArray expectedJSONArray = new JSONArray()
                .put(new JSONObject(mapper.writeValueAsString(
                        new EvaluationQuestion(TestUtil.EVALUATION_UUID_1, TestUtil.EVALUATION_TEXT_1))))
                .put(new JSONObject(mapper.writeValueAsString(
                        new EvaluationQuestion(TestUtil.EVALUATION_UUID_2, TestUtil.EVALUATION_TEXT_2))))
                .put(new JSONObject(mapper.writeValueAsString(
                        new EvaluationQuestion(TestUtil.EVALUATION_UUID_3, TestUtil.EVALUATION_TEXT_3))))
                .put(new JSONObject(mapper.writeValueAsString(
                        new EvaluationQuestion(TestUtil.EVALUATION_UUID_4, TestUtil.EVALUATION_TEXT_4))));

        JSONAssert.assertEquals(expectedJSONArray, resultJSON, true);

    }

    private CandidateController withMockedServices(CandidateService mockedCandidateServer, Candidate candidate) throws Exception {
        UserService mockedUserServer = mock(UserService.class);
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        User mockedUser = mock(User.class);
        CandidateController controller =
                new CandidateController(mockedCandidateServer, mockedUserServer, mockedRequest);
        when(mockedUserServer.validateUserSession(mockedRequest, User.Type.CANDIDATE)).thenReturn(mockedUser);
        when(mockedCandidateServer.getLatestApplication(1L)).thenReturn(new Application());
        when(mockedCandidateServer.updateCandidateProfile("", candidate)).thenReturn(new Candidate());
        when(mockedUser.getUserId()).thenReturn(1L);
        when(mockedUser.getUseruuid()).thenReturn("testuseruuid");
        when(mockedUser.getUserSesson()).thenReturn(mock(HttpSession.class));
        when(mockedRequest.getSession(false)).thenReturn(mock(HttpSession.class));
        return controller;
    }

    private JSONArray candidateAnswers() {
        JSONArray answers = new JSONArray();
        JSONObject answer1 = new JSONObject();
        answer1.put("questionId", "question1uuid");
        answer1.put("answer", 2);
        answers.put(answer1);
        JSONObject answer2 = new JSONObject();
        answer2.put("questionId", "question2uuid");
        answer2.put("answer", 3);
        answers.put(answer2);
        return answers;
    }

    private void candidatesLoginAndApply() throws Exception {
        Candidate c1 = new Candidate();
        c1.setEmail("pCNBnv@gmail.com");
        c1.setPassword("55ReQFZX");
        MvcResult result = mockMvc.perform(post("/candidate/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(c1)))
                .andExpect(status().isOk())
                .andReturn();
        candidate1UUID = new JSONObject(result.getResponse().getContentAsString()).getString("id");

        setup();
        mockHttpSession.setAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE,
                "{\"jobfairUserId\":1," +
                        "\"sessionId\":\"testSessionId\"," +
                        "\"jobfairUserUUID\":\"testuseruuid\"," +
                        "\"userId\":\"24\"," +
                        "\"avatar\":\"1\"," +
                        "\"email\":\"pCNBnv@gmail.com\"," +
                        "\"jobfairCompanyId\":\"1\"}");
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CandidateControllerTest.CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication()))
                .param("questionAnswers", candidateAnswers().toString()))
                .andExpect(status().isOk());

        setup();
        Candidate c2 = new Candidate();
        c2.setEmail("CBfDuY@gmail.com");
        c2.setPassword("GWTYZ3xL");
        result = mockMvc.perform(post("/candidate/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(c2)))
                .andExpect(status().isOk())
                .andReturn();
        candidate2UUID = new JSONObject(result.getResponse().getContentAsString()).getString("id");

        setup();
        mockHttpSession.setAttribute(JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE,
                "{\"jobfairUserId\":2," +
                        "\"sessionId\":\"testSessionId\"," +
                        "\"jobfairUserUUID\":\"testuseruuid2\"," +
                        "\"userId\":\"24\"," +
                        "\"avatar\":\"1\"," +
                        "\"email\":\"CBfDuY@gmail.com\"," +
                        "\"jobfairCompanyId\":\"1\"}");
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CandidateControllerTest.CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication()))
                .param("questionAnswers", candidateAnswers().toString()))
                .andExpect(status().isOk());

        setup();
    }
}
