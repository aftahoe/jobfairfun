package job.fair.jobfair.jpa.controller;

import job.fair.commons.ObjectConverter;
import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.Company;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.jpa.repository.InterviewerRepository;
import job.fair.jobfair.jpa.service.CandidateService;
import job.fair.jobfair.jpa.service.CandidateServiceImpl;
import job.fair.jobfair.jpa.service.CompanyService;
import job.fair.jobfair.jpa.service.UserService;
import job.fair.jobfair.jpa.service.UserServiceImpl;
import job.fair.jobfair.testutil.TestUtil;
import job.fair.tutormeet.TutorMeet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_ROLE;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_USER_ID;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_USER_STATUS;
import static job.fair.jobfair.JobfairConstants.SessionParms.JOBFAIR_USER_UUID;
import static job.fair.jobfair.JobfairConstants.SessionParms.SESSION_ID;
import static job.fair.jobfair.JobfairConstants.SessionParms.SESSION_USER_ATTRIBUTE;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_APITOKEN;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_EMAIL;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_NAME;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_RETURNCODE;
import static job.fair.jobfair.JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_USERSN;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by annawang on 1/26/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml",
                       "classpath:spring/mvc-dispatcher-servlet.xml"})
@WebAppConfiguration
@ActiveProfiles("jpa")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CandidateControllerTest {

    public static final String CANDIDATE_APPLY_URI = "/candidates/application/job1uuid";
    public static final String CANDIDATE_SIGNUP_URI = "/candidates/signup";
    private static String candidateUUID;
    private static String jobUUID = "testJobUUID";

    @Autowired
    protected CompanyService companyService;
    @Mock
    CandidateServiceImpl mockedCandidateService;
    @Mock
    UserServiceImpl mockedUserService;
    @Mock
    CompanyService mockedCompanyService;
    @InjectMocks
    CandidateController controllerUnderTest;
    @Mock
    HttpServletRequest mockedRequest;
    @Mock
    User mockedUser;
    @Autowired
    private InterviewerRepository interviewerRepository;
    @Autowired
    private CandidateController candidateController;
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CandidateRepository candidateRepository;
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
        mockHttpSession = new MockHttpSession(context.getServletContext(), "testSessionId");
        JSONObject userAttribute = new JSONObject();
        userAttribute.put("jobfairUserId", 1);
        userAttribute.put("sessionId", "testSessionId");
        userAttribute.put("jobfairUserUUID", candidateUUID == null ? "fakeUUID" : candidateUUID);
        userAttribute.put("userId", 24);
        userAttribute.put("avatar", "1");
        userAttribute.put("email", TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        mockHttpSession.setAttribute(SESSION_USER_ATTRIBUTE, userAttribute.toString());
    }

    // ============= login  ===================
    @Test
    public void login() throws Exception {
        mockHttpSession.setNew(true);
        MvcResult result = mockMvc.perform(post("/candidates/login")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(TestUtil.createTutorMeetCandidate())))
                .andExpect(status().isOk())
                .andReturn();

        // The session after login
        JSONObject userSessionJSON = new JSONObject(
                (String) result.getRequest().getSession(false).getAttribute(SESSION_USER_ATTRIBUTE));


        // Make sure all information we care are in the session object
        assertEquals(0, userSessionJSON.getInt(TUTORMEET_LOGINOBJECT_RETURNCODE));
        Assert.hasLength(userSessionJSON.getString(TUTORMEET_LOGINOBJECT_APITOKEN));
        Assert.hasLength(userSessionJSON.getString(SESSION_ID));
        Assert.hasLength(userSessionJSON.getString(TUTORMEET_LOGINOBJECT_EMAIL));
        Assert.hasLength(userSessionJSON.getString(TUTORMEET_LOGINOBJECT_NAME));
        candidateUUID = userSessionJSON.getString(JOBFAIR_USER_UUID);
        Assert.hasLength(candidateUUID);
        assertEquals(1L, userSessionJSON.getLong(JOBFAIR_USER_ID));
        assertEquals(0, userSessionJSON.getInt(JOBFAIR_USER_STATUS));
        assertEquals(24, userSessionJSON.getInt(TUTORMEET_LOGINOBJECT_USERSN));
        assertEquals(User.Type.CANDIDATE.name(), userSessionJSON.getString(JOBFAIR_ROLE));


        // Return object
        JSONObject resultJSON = new JSONObject(result.getResponse().getContentAsString());
        JSONObject expectedJSON = new JSONObject()
                .put("id", candidateUUID)
                .put("email", TestUtil.TUTORMEET_TEST_EMAIL_VALUE)
                .put("name", TestUtil.TUTORMEET_TEST_NAME_VALUE)
                .put("role", User.Type.CANDIDATE.name())
                .put("avatar", "24122")
                .put("iconPathPrefix", TutorMeet.TUTORMEET_AVATAR_PREFIX + "24_");
        JSONAssert.assertEquals(expectedJSON, resultJSON, true);
    }

    @Test
    public void loginFailed() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setPassword("failedpassword");

        mockMvc.perform(post("/candidates/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.url").value(containsString("/candidates/login")));
    }

    @Test
    public void loginWithoutEmail() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setEmail("");
        mockMvc.perform(post("/candidates/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's email is required")));
    }

    @Test
    public void loginWithNullEmail() throws Exception {
        Candidate existingCandidate = new Candidate();
        existingCandidate.setName("testuser");

        mockMvc.perform(post("/candidates/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's email is required")));
    }

    @Test
    public void loginWithoutPassword() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setPassword("");
        mockMvc.perform(post("/candidates/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's password is required")));
    }

    @Test
    public void loginWithNullPassword() throws Exception {
        Candidate existingCandidate = new Candidate();
        existingCandidate.setEmail("testemail");

        mockMvc.perform(post("/candidates/login")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's password is required")));
    }

    //============== logout tests=====================
    @Test
    public void logout() throws Exception {

        MvcResult result = mockMvc.perform(post("/candidates/logout")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(TestUtil.createTutorMeetCandidate())))
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
        Candidate can = new Candidate();
        can.setId(1L);
        can.setUuid("testuseruuid");
        can.setStatus(Candidate.CandidateStatus.AWAY);
        Mockito.verify(mockedCandidateServer, times(1)).updateCandidateStatus(can);
    }

    @Test
    public void logoutFailed() throws Exception {
        MvcResult result = mockMvc.perform(post("/candidates/logout")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(TestUtil.createTutorMeetCandidate())))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String messageString = new JSONObject(result.getResponse().getContentAsString()).getString("message");
        assertEquals("Session Authentication Failed: Session is Null", messageString);
    }

    //============== singup tests=====================
    @Test
    public void signup() throws Exception {

        Candidate c = TestUtil.createTutorMeetSignUpCandidate();
        c.setEmail("signuptest@gmail.com");

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(c)))
                .andExpect(status().isOk());

        //make sure the signup is called
        CandidateService mockedCandidateServer = mock(CandidateService.class);
        CandidateController controller =
                new CandidateController(mockedCandidateServer, mock(UserService.class), mock(HttpServletRequest.class));
        controller.candidateSignup(c);
        Mockito.verify(mockedCandidateServer, times(1)).signup(c);
    }

    @Test
    public void signupFailedWithExistingUser() throws Exception {
        Candidate c = TestUtil.createTutorMeetSignUpCandidate();

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(c)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.url").value(containsString("/candidates/signup")))
                .andExpect(jsonPath("$.message").value("User has already signup"));
    }

    @Test
    public void signupWithoutEmail() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setEmail("");
        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's email is required")));
    }

    @Test
    public void signupWithNullEmail() throws Exception {
        Candidate existingCandidate = new Candidate();
        existingCandidate.setName(TestUtil.TUTORMEET_TEST_NAME_VALUE);

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's email is required")));
    }

    @Test
    public void signupWithoutPassword() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setPassword("");
        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's password is required")));
    }

    @Test
    public void singupWithNullPassword() throws Exception {
        Candidate existingCandidate = new Candidate();
        existingCandidate.setEmail(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's password is required")));
    }

    @Test
    public void signupWithoutName() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setName("");

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's name is required")));
    }

    @Test
    public void singupWithNullName() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's name is required")));
    }

    @Test
    public void signupWithoutLocale() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setName(TestUtil.TUTORMEET_TEST_NAME_VALUE);
        existingCandidate.setLocale("");

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's locale is required")));
    }

    @Test
    public void singupWithNullLocale() throws Exception {
        Candidate existingCandidate = TestUtil.createTutorMeetCandidate();
        existingCandidate.setName(TestUtil.TUTORMEET_TEST_NAME_VALUE);

        mockMvc.perform(post(CANDIDATE_SIGNUP_URI)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(existingCandidate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's locale is required")));
    }

    // ===================== Status ================================
    @Test
    public void testGetStatus() throws Exception {
        mockMvc.perform(get("/candidates/status").session(mockHttpSession))
                .andExpect(status().isOk())
                .andExpect(content().bytes("{\"status\":1}".getBytes()));
    }

    @Test
    public void testPutStatus() throws Exception {
        JSONObject status = new JSONObject().put("status", 0);

        MvcResult returnResult = mockMvc.perform(put("/candidates/status")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(status.toString().getBytes()))
                .andExpect(status().isOk())
                .andReturn();


        JSONObject returnJSON = new JSONObject(returnResult.getResponse().getContentAsString());
        JSONObject expectedJSON = new JSONObject()
                .put("status", 0)
                .put("email", TestUtil.TUTORMEET_TEST_EMAIL_VALUE)
                .put("name", TestUtil.TUTORMEET_TEST_NAME_VALUE)
                .put("id", candidateUUID);
        JSONAssert.assertEquals(expectedJSON, returnJSON, true);
    }

    @Test
    public void testPutStatusFailed_emptyJSON() throws Exception {
        JSONObject status = new JSONObject();

        mockMvc.perform(put("/candidates/status")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(status.toString().getBytes()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testPutStatusFailed_statusOutOfBound() throws Exception {
        JSONObject status = new JSONObject();
        status.put("status", 5);

        mockMvc.perform(put("/candidates/status")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(status.toString().getBytes()))
                .andExpect(status().isUnprocessableEntity());
    }

    // ===================== favorite job ================================
    @Test
    public void testFavoriteJobsGet() throws Exception {
        MvcResult result = mockMvc.perform(get("/candidates/favoritejobs").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray jobsArray = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(0, jobsArray.length());
    }

    @Test
    public void testFavoriteJobsPut() throws Exception {
        Job job = this.companyService.getJob("job5uuid");
        MvcResult result = mockMvc.perform(put("/candidates/favoritejobs/job5uuid")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray jobsArray = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jobsArray.length());

        JSONObject expectedJSON = new JSONObject()
                .put("companyName", job.getCompany().getName())
                .put("id", job.getUuid())
                .put("location", job.getLocation())
                .put("positionName", job.getPositionName())
                .put("positionCount", job.getPositionCount())
                .put("positionNumber", 0)
                .put("compensation", 190000);
        JSONAssert.assertEquals(expectedJSON, (JSONObject) jobsArray.get(0), true);
    }

    @Test
    public void testFavoriteJobsPutGet() throws Exception {
        Job job = this.companyService.getJob("job5uuid");
        mockMvc.perform(put("/candidates/favoritejobs/job1uuid")
                .session(mockHttpSession)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

        setup();
        MvcResult result = mockMvc.perform(get("/candidates/favoritejobs").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray jobsArray = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(2, jobsArray.length());

        JSONObject expectedJSON = new JSONObject()
                .put("companyName", job.getCompany().getName())
                .put("id", job.getUuid())
                .put("positionName", job.getPositionName())
                .put("positionCount", job.getPositionCount())
                .put("location", job.getLocation())
                .put("positionNumber", 0)
                .put("compensation", 190000);
        JSONAssert.assertEquals(expectedJSON, (JSONObject) jobsArray.get(0), true);
    }

    @Test
    public void testFavoriteJobs_Remove() throws Exception {
        Job job = this.companyService.getJob("job5uuid");

        MvcResult result = mockMvc.perform(delete("/candidates/favoritejobs/job1uuid").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray jobsArray = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jobsArray.length());

        JSONObject expectedJSON = new JSONObject()
                .put("companyName", job.getCompany().getName())
                .put("id", job.getUuid())
                .put("positionName", job.getPositionName())
                .put("positionCount", job.getPositionCount())
                .put("location", job.getLocation())
                .put("positionNumber", 0)
                .put("compensation", 190000);
        JSONAssert.assertEquals(expectedJSON, (JSONObject) jobsArray.get(0), true);
    }

    // ====================  Candidate Apply============

    @Test
    public void testApply() throws Exception {
        //create company for testing
        createCompany();

        MvcResult returnResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication()))
                .param("questionAnswers", candidateAnswers().toString()))
                .andExpect(status().isOk())
                .andReturn();

        JSONObject returnObject = new JSONObject(returnResult.getResponse().getContentAsString());
        JSONObject expectedJSON = new JSONObject()
                .put("companyName", TestUtil.TUTORMEET_COMPANY_NAME)
                .put("positionName", "Front-End Developer")
                .put("positionNumber", 0)
                .put("description", "TutorGroup is seeking a creative Front End Developer to drive user experience innovations for apple.com. This developer will not only be responsible for defining the architectural strategy for front end technologies - including HTML5, CSS3 and JavaScript - but for evangelizing that technology across the team and TutorGroup as a whole.\n" +
                        "- Comfortable with source version control software (CVS, SVN, and Git)\n" +
                        "- Well -versed in fundamental visual and interactive design discipline\n" +
                        "- Strive to use web standards to build solutions using semantic markup and CSS")
                .put("location", "Santa Clara, CA")
                .put("compensation", 10000000)
                .put("id", "job1uuid");
        JSONAssert.assertEquals(expectedJSON, returnObject, true);

    }

    @Test
    public void testApplyGet() throws Exception {
        MvcResult result = mockMvc.perform(get("/candidates/application").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONArray resultJSON = new JSONArray(result.getResponse().getContentAsString());
        JSONArray expectedJSONArray = new JSONArray()
                .put(new JSONObject()
                        .put("companyName", TestUtil.TUTORMEET_COMPANY_NAME)
                        .put("queuePosition", 1)
                        .put("positionName", "Front-End Developer")
                        .put("id", "job1uuid"));
        JSONAssert.assertEquals(expectedJSONArray, resultJSON, true);
    }

    @Test
    public void testApplyRemove() throws Exception {
        mockMvc.perform(delete(CANDIDATE_APPLY_URI).session(mockHttpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$message").value("Delete the user from the job queue job1uuid"));
    }

    @Test
    public void testApplyRemoveGet() throws Exception {
        mockMvc.perform(get("/candidates/application").session(mockHttpSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testApplyWithoutResume() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication()))
                .param("questionAnswers", candidateAnswers().toString()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("resume file is required")));
    }

    @Test
    public void testApplyWithInvlidJSON() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("A JSONObject text must begin with")));
    }

    @Test
    public void testApplyWithInvlidJobId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/candidates/application/12345")
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid Job ID: 12345")));
    }

    @Test
    public void testApplyWithInvlidAnswers() throws Exception {

        JSONArray answers = new JSONArray();
        JSONObject answer1 = new JSONObject();
        answer1.put("questionId", 1);
        answer1.put("answer", 2);
        answers.put(answer1);

        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", ObjectConverter.toJSONString(TestUtil.createApplication()))
                .param("questionAnswers", answers.toString()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("2 answers are expected but there are 1 got!")));
    }

    @Test
    public void testApplyWithoutInstruction() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", "{\"id\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("JSONObject[\"introduction\"] not found")));
    }


    @Test
    public void testApplyWithEmptyInstruction() throws Exception {
        String candidateJSONString = "{\"id\":1,\"introduction\":\"\"}";

        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's introduction is required")));
    }

    @Test
    public void testApplyWithoutWorkethic() throws Exception {

        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\"}";

        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("JSONObject[\"workethic\"] not found")));
    }


    @Test
    public void testApplyWithEmptyWorkethic() throws Exception {
        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"\"}";

        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Candidate's workethic is required")));
    }


    @Test
    public void testApplyWithoutSkills() throws Exception {
        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"my workthic\"," +
                "\"cellphone\":\"12345\",\"wechatid\":\"my wechatid\"}";

        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("JSONObject[\"skills\"] not found")));
    }

    @Test
    public void testApplyWithEmptySkills() throws Exception {
        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"my workthic\"," +
                "\"cellphone\":\"12345\",\"wechatid\":\"my wechatid\",\"skills\":\"\"}";

        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid skills JSONArray")));
    }

    @Test
    public void testApplyWithEmptySkillsArray() throws Exception {
        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"my workthic\"," +
                "\"cellphone\":\"12345\",\"wechatid\":\"my wechatid\",\"skills\":[]}";
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("Skill JSONArray is required")));
    }

    @Test
    public void testApplyWithEmptyExperience() throws Exception {

        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"my workthic\"," +
                "\"cellphone\":\"12345\",\"wechatid\":\"my wechatid\",\"skills\":[\"JAVA\"]}";
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("JSONObject[\"experience\"] not found")));
    }

    @Test
    public void testApplyWithEmptySalary() throws Exception {

        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"my workthic\"," +
                "\"cellphone\":\"12345\",\"wechatid\":\"my wechatid\",\"skills\":[\"JAVA\"], \"experience\":3}";
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("JSONObject[\"salary\"] not found")));
    }


    @Test
    public void testApplyWithInvalidSkillsArray() throws Exception {

        String candidateJSONString = "{\"id\":1,\"introduction\":\"my introduction\",\"workethic\":\"my workthic\"," +
                "\"cellphone\":\"12345\",\"wechatid\":\"my wechatid\",\"skills\":[{\"skill\":\"JAVA\"}], " +
                "\"experience\":3,\"salary\":30000}";
        mockMvc.perform(MockMvcRequestBuilders.fileUpload(CANDIDATE_APPLY_URI)
                .file(TestUtil.testResumeMultipartFile())
                .session(mockHttpSession)
                .param("candidate", candidateJSONString))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message").value(containsString("not a string")));
    }


    @Test
    public void testSearchInterviewedJobsByTime() throws Exception {
        Candidate c = this.candidateService.getCandidateByEmail(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        Interviewer in = interviewerRepository.addInterviewer(TestUtil.createInterviewer());
        addThreeReports(c, in);

        MvcResult result = mockMvc.perform(get("/candidates/jobs/interviewed")
                .param("startTime", Long.toString(TestUtil.getTodayStartOfDay().getTime()))
                .param("endTime", Long.toString(TestUtil.getTodayEndOfDay().getTime()))
                .session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(2, new JSONArray(result.getResponse().getContentAsString()).length());
    }

    @Test
    public void testSearchRecommendedJobs() throws Exception {
        MvcResult result = mockMvc.perform(get("/candidates/jobs/recommendation").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(1, new JSONArray(result.getResponse().getContentAsString()).length());
    }

    @Test
    public void zgetCandidateLatestApplication() throws Exception {
        MvcResult result = mockMvc.perform(get("/candidates/latestApplication").session(mockHttpSession))
                .andExpect(status().isOk())
                .andReturn();

        JSONAssert.assertEquals(profileCandidate(), new JSONObject(result.getResponse().getContentAsString()), true);

        //make sure getLatestApplication is called
//        CandidateService mockedCandidateServer = mock(CandidateService.class);
//        CandidateController controller = withMockedServices(mockedCandidateServer, null);
//        controller.getCandidateProfile();
//        Mockito.verify(mockedCandidateServer, times(1)).getLatestApplication(1L);
    }


//    @Test
//    public void zputCandidateProfile() throws Exception {
//        Candidate updateCandidate = new Candidate();
//        updateCandidate.setIntroduction("this is the new intro");
//
//        MvcResult result = mockMvc.perform(put("/candidate/profile").session(mockHttpSession)
//                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
//                .content(convertObjectToJsonBytes(updateCandidate)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        JSONObject expectedCandidate = profileCandidate().put("introduction", "this is the new intro");
//        JSONAssert.assertEquals(expectedCandidate, new JSONObject(result.getResponse().getContentAsString()), true);
//
//        //make sure updateCandidateProfile is called
//        CandidateService mockedCandidateServer = mock(CandidateService.class);
//        CandidateController controller = withMockedServices(mockedCandidateServer, updateCandidate);
//        controller.updateCandidateProfile(updateCandidate);
//        Mockito.verify(mockedCandidateServer, times(1)).updateCandidateProfile(TUTORMEET_TEST_EMAIL_VALUE, updateCandidate);
//    }

    private JSONObject profileCandidate() {
        return new JSONObject()
                .put("id", candidateUUID)
                .put("name", TestUtil.TUTORMEET_TEST_NAME_VALUE)
                .put("email", TestUtil.TUTORMEET_TEST_EMAIL_VALUE)
                .put("introduction", TestUtil.TEST_INTRODUCTION_VALUE)
                .put("workethic", TestUtil.TEST_ETHIC_VALUE)
                .put("cellphone", TestUtil.TEST_PHONE_VALUE)
                .put("wechatid", TestUtil.TEST_WE_CHAT_ID_VALUE)
                .put("experience", TestUtil.TEST_EXPERIENCE_VALUE)
                .put("salary", TestUtil.TEST_SALARY_VALUE)
                .put("status", 0)
                .put("skills", new JSONArray().put("JAVA").put("HTML"));
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
        when(mockedUser.getEmail()).thenReturn(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
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

    private void createCompany() {
        Company company = TestUtil.createTestCompany();

        companyService.addCompany(company);
    }

    public void addThreeReports(Candidate candidate, Interviewer in) throws IOException, URISyntaxException, CandidateException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Job job1 = this.companyService.getJob("job1uuid");
        Job job2 = this.companyService.getJob("job2uuid");
        Job job3 = this.companyService.getJob("job3uuid");

        Map<String, Integer> evlResult = new HashMap<>();
        for (EvaluationQuestion q : this.companyService.getEvaluationQuestions(1L)) {
            evlResult.put(q.getUuid(), 1);
        }

        Application application1 = TestUtil.createApplication();
        application1.setJob(job1);
        application1.setCandidate(candidate);
        this.candidateRepository.addApplication(application1);

        Application application2 = TestUtil.createApplication();
        application2.setJob(job2);
        application2.setCandidate(candidate);
        this.candidateRepository.addApplication(application2);

        Application application3 = TestUtil.createApplication();
        application3.setJob(job3);
        application3.setCandidate(candidate);
        this.candidateRepository.addApplication(application3);

        EvaluationReport report = new EvaluationReport();
        report.setInterviewer(in);
        report.setScores(evlResult);
        report.setApplication(application1);
        report.setRecommendationType(EvaluationReport.Type.NOTRECOMMENDED);
        report.setNote("note");
        this.companyService.putEvaluationResult(report);
        EvaluationReport report2 = new EvaluationReport(report);
        report2.setApplication(application2);
        this.companyService.putEvaluationResult(report2);
        EvaluationReport report3 = new EvaluationReport(report);
        report3.setApplication(application3);
        report3.setRecommendationType(EvaluationReport.Type.RECOMMENDED);
        this.companyService.putEvaluationResult(report3);
        this.companyRepository.updateEvaluationReportTimestamp(3);
    }
}
