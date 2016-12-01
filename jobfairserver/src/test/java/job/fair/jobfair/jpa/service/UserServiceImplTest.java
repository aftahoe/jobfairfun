package job.fair.jobfair.jpa.service;

import job.fair.data.Page;
import job.fair.jobfair.Exception.CandidateException;
import job.fair.jobfair.JobfairConstants;
import job.fair.jobfair.jpa.entity.Application;
import job.fair.jobfair.jpa.entity.Candidate;
import job.fair.jobfair.jpa.entity.EvaluationQuestion;
import job.fair.jobfair.jpa.entity.EvaluationReport;
import job.fair.jobfair.jpa.entity.Interviewer;
import job.fair.jobfair.jpa.entity.Job;
import job.fair.jobfair.jpa.entity.User;
import job.fair.jobfair.jpa.repository.CandidateRepository;
import job.fair.jobfair.jpa.repository.CompanyRepository;
import job.fair.jobfair.testutil.TestUtil;
import job.fair.tutormeet.TutorMeet;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by annawang on 2/23/16.
 */
@ContextConfiguration({"classpath:spring/spring-config-test.xml", "classpath:spring/tools-config.xml", "classpath:spring/mvc-dispatcher-servlet.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("jpa")
//@TransactionConfiguration(defaultRollback = true)
//@Transactional
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class UserServiceImplTest {

    public static final String UPDATED_TOKEN = "updatedToken";
    public static final String UPDATED_API_TOKEN = "updatedApiToken";
    public static final int TUTORMEET_USER_SN_VALUE = 24;
    public static final String TUTORMEET_AVATAR_VALUE = "1";
    @Autowired
    protected CandidateServiceImpl candidateService;
    @Autowired
    protected CompanyRepository companyRepository;
    @Autowired
    protected CandidateRepository candidateRepository;
    @Autowired
    protected InterviewerServiceImpl interviewerService;
    @Autowired
    protected CompanyServiceImpl companyService;

    @Autowired
    protected UserServiceImpl userService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private Candidate candidate;

    private User user;

    @Before
    public void setup() throws Exception {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");

        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(characterEncodingFilter, "/*").build();

        candidate = TestUtil.createCandidate();
        user = TestUtil.createTutorMeetUser();
    }

    @Test
    public void loginNewInterviewer() throws Exception {
        this.userService.login(user.getEmail(), user.getPassword(), mockedRequest(), User.Type.INTERVIEWER);

        Interviewer in = this.interviewerService.getInterviewerByEmail(user.getEmail());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, in.getEmail());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_NAME_VALUE, in.getName());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_PASSWORD, in.getPassword());
        assertEquals(24, in.getTutorMeetUserSN());
        Assert.hasLength(in.getTutorMeetToken());
        Assert.hasLength(in.getTutorMeetApitoken());
        Assert.hasLength(in.getUuid());
    }

    @Test
    public void loginWithExistingInterviewer() throws Exception {
        UserService service = mockedTutorMeetWithUpdatedTokens();
        service.login(user.getEmail(), user.getPassword(), mockedRequest(), User.Type.INTERVIEWER);

        Interviewer in = this.interviewerService.getInterviewerByEmail(user.getEmail());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, in.getEmail());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_NAME_VALUE, in.getName());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_PASSWORD, in.getPassword());
        assertEquals(TUTORMEET_USER_SN_VALUE, in.getTutorMeetUserSN());
        assertEquals(UPDATED_TOKEN, in.getTutorMeetToken());
        assertEquals(UPDATED_API_TOKEN, in.getTutorMeetApitoken());
        Assert.hasLength(in.getUuid());
    }

    @Test
    public void loginNewCandidate() throws Exception {
        this.userService.login(user.getEmail(), user.getPassword(), mockedRequest(), User.Type.CANDIDATE);

        Candidate candidate = this.candidateService.getCandidateByEmail(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, candidate.getEmail());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_NAME_VALUE, candidate.getName());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_PASSWORD, candidate.getPassword());
        assertEquals(24, candidate.getTutorMeetUserSN());
        assertEquals(0, candidate.getStatus().getOrdinal());
        Assert.hasLength(candidate.getTutorMeetToken());
        Assert.hasLength(candidate.getTutorMeetApitoken());
        Assert.hasLength(candidate.getName());
    }

    @Test
    public void loginWithExistingCandidate() throws Exception {
        UserService service = mockedTutorMeetWithUpdatedTokens();
        service.login(user.getEmail(), user.getPassword(), mockedRequest(), User.Type.CANDIDATE);

        Candidate candidate = this.candidateService.getCandidateByEmail(TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, candidate.getEmail());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_NAME_VALUE, candidate.getName());
        org.junit.Assert.assertEquals(TestUtil.TUTORMEET_TEST_PASSWORD, candidate.getPassword());
        assertEquals(TUTORMEET_USER_SN_VALUE, candidate.getTutorMeetUserSN());
        assertEquals(UPDATED_TOKEN, candidate.getTutorMeetToken());
        assertEquals(UPDATED_API_TOKEN, candidate.getTutorMeetApitoken());
        Assert.hasLength(candidate.getUuid());

    }

    @Test(expected = AuthenticationException.class)
    public void loginFailed() throws Exception {
        this.userService.login(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, "fackpassword", mockedRequest(), User.Type.INTERVIEWER);
    }

    @Test
    public void testCandidateGetInterviewJobs() throws Exception {
        Candidate c = this.candidateRepository.getCandidate(1L);
        companyService.addCompany(TestUtil.createTestCompany());
        addThreeReports(c);

        Timestamp starttime = TestUtil.getTodayStartOfDay();
        Timestamp endtime = TestUtil.getTodayEndOfDay();
        List<Job> result = this.userService.getInterviewedJobs(c.getId(), User.Type.CANDIDATE,
                starttime, endtime);
        assertEquals(2, result.size());

        assertEquals(2, result.get(0).getId());
        assertEquals(1, result.get(1).getId());

        Timestamp yesStartTime = TestUtil.getYesterdayStartOfDay();
        Timestamp yesEndTime = TestUtil.getYesterdayEndOfDay();
        result = this.userService.getInterviewedJobs(c.getId(), User.Type.CANDIDATE,
                yesStartTime, yesEndTime);
        assertEquals(1, result.size());

        assertEquals(3, result.get(0).getId());

        result = this.userService.getInterviewedJobs(c.getId(), User.Type.CANDIDATE,
                yesStartTime, endtime);
        assertEquals(3, result.size());

        assertEquals(2, result.get(0).getId());
        assertEquals(1, result.get(1).getId());
        assertEquals(3, result.get(2).getId());

    }

    @Test
    public void testCandidateGetInterviewJobs_empty() throws Exception {
        List<Job> result = this.userService.getInterviewedJobs(5L, User.Type.CANDIDATE,
                TestUtil.getTodayStartOfDay(), TestUtil.getTodayEndOfDay());
        assertEquals(0, result.size());
    }

    @Test
    public void testInterviewerGetInterviewJobs() throws Exception {
        Interviewer in = this.interviewerService.getInterviewer(1L);

        List<Job> result = this.userService.getInterviewedJobs(in.getId(), User.Type.INTERVIEWER,
                TestUtil.getTodayStartOfDay(), TestUtil.getTodayEndOfDay());
        assertEquals(2, result.size());

        assertEquals(2, result.get(0).getId());
        assertEquals(1, result.get(1).getId());

        result = this.userService.getInterviewedJobs(in.getId(), User.Type.INTERVIEWER,
                TestUtil.getYesterdayStartOfDay(), TestUtil.getYesterdayEndOfDay());
        assertEquals(1, result.size());

        assertEquals(3, result.get(0).getId());

        result = this.userService.getInterviewedJobs(in.getId(), User.Type.INTERVIEWER,
                TestUtil.getYesterdayStartOfDay(), TestUtil.getTodayEndOfDay());
        assertEquals(3, result.size());

        assertEquals(2, result.get(0).getId());
        assertEquals(1, result.get(1).getId());
        assertEquals(3, result.get(2).getId());

    }

    @Test
    public void testInterviewerGetInterviewJobs_empty() throws Exception {
        List<Job> result = this.userService.getInterviewedJobs(5L, User.Type.INTERVIEWER,
                TestUtil.getTodayStartOfDay(), TestUtil.getTodayEndOfDay());
        assertEquals(0, result.size());
    }

    @Test
    public void testUserGetInterviewerGetInterviewedResult() throws Exception {
        Pageable pageable = new PageRequest(0, 4);
        Page<EvaluationReport> reports = this.interviewerService.getInterviewerInterviewJobs(1L, "", pageable);

        assertEquals(1, reports.getTotalElements());
    }

    public void addThreeReports(Candidate candidate) throws IOException, URISyntaxException, CandidateException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Job job1 = this.companyService.getJob("job1uuid");
        Job job2 = this.companyService.getJob("job2uuid");
        Job job3 = this.companyService.getJob("job3uuid");
        this.userService.login(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, TestUtil.TUTORMEET_TEST_PASSWORD, mockedRequest(),
                User.Type.INTERVIEWER);
        Interviewer in = this.interviewerService.getInterviewer(1L);

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
        report.setApplication(application1);
        report.setScores(evlResult);
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


    private HttpServletRequest mockedRequest() {
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        HttpSession mockedSession = mock(HttpSession.class);
        when(mockedRequest.getSession(true)).thenReturn(mockedSession);
        when(mockedRequest.getSession()).thenReturn(mockedSession);
        when(mockedSession.isNew()).thenReturn(true);
        when(mockedSession.getId()).thenReturn("testSessionId");
        return mockedRequest;
    }


    private UserService mockedTutorMeetWithUpdatedTokens() throws URISyntaxException, IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TutorMeet mockedTutorMeet = mock(TutorMeet.class);
        UserService service = new UserServiceImpl(mockedTutorMeet, this.candidateService.getCandidateRepository(),
                this.userService.getCompanyRepository(), this.userService.getInterviewerRepository());
        JSONObject jsonWithNewToken = new JSONObject();
        jsonWithNewToken.put(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_NAME, TestUtil.TUTORMEET_TEST_NAME_VALUE);
        jsonWithNewToken.put(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_TOKEN, UPDATED_TOKEN);
        jsonWithNewToken.put(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_APITOKEN, UPDATED_API_TOKEN);
        jsonWithNewToken.put(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_USERSN, TUTORMEET_USER_SN_VALUE);
        jsonWithNewToken.put(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_AVATAR, TUTORMEET_AVATAR_VALUE);
        jsonWithNewToken.put(JobfairConstants.SessionParms.TUTORMEET_LOGINOBJECT_EMAIL, TestUtil.TUTORMEET_TEST_EMAIL_VALUE);
        jsonWithNewToken.put("returnCode", 0);
        when(mockedTutorMeet.login(TestUtil.TUTORMEET_TEST_EMAIL_VALUE, TestUtil.TUTORMEET_TEST_PASSWORD)).thenReturn(jsonWithNewToken);
//        when(mockedInterviewerRepository.isExistingInterviewer(TUTORMEET_TEST_EMAIL_VALUE)).thenReturn(true);
//        when(mockedInterviewerRepository.updateInterviewer(any(Interviewer.class))).thenCallRealMethod();
//        Interviewer interviewer = new Interviewer();
//        interviewer.setEmail(TUTORMEET_TEST_EMAIL_VALUE);
//        interviewer.setId(1L);
//        interviewer.setTutorMeetApitoken("pre-apitoken");
//        interviewer.setTutorMeetToken("pre-token");
//        interviewer.setName(TUTORMEET_TEST_NAME_VALUE);
//        when(mockedInterviewerRepository.getInterviewerByEmail(TUTORMEET_TEST_EMAIL_VALUE)).thenReturn(interviewer);
//        Company company = new Company();
//        company.setId(1L);
//        when(mockedCompanyRepository.getCompanyByDomain("tutormeet.com")).thenReturn(company);
        return service;
    }
}
